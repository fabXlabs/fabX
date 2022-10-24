package cloud.fabX.fabXaccess.device.ws

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.getOrHandle
import arrow.core.left
import arrow.core.right
import arrow.core.toOption
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.DeviceId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.Error.DeviceCommunicationSerializationError
import cloud.fabX.fabXaccess.common.rest.readDeviceAuthentication
import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import io.ktor.websocket.send
import java.util.Collections
import kotlin.random.Random
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class DeviceWebsocketController(
    loggerFactory: LoggerFactory,
    private val commandHandler: DeviceCommandHandler,
    private val notificationHandler: DeviceNotificationHandler,
    private val deviceReceiveTimeoutMillis: Long
) {
    private val logger = loggerFactory.invoke(this::class.java)

    private val connections: MutableMap<DeviceId, DefaultWebSocketSession> =
        Collections.synchronizedMap(HashMap())

    private val responseChannels: MutableMap<Int, Channel<DeviceResponse>> =
        Collections.synchronizedMap(HashMap())

    val routes: Route.() -> Unit = {
        route("/device") {
            webSocket("ws") {
                readDeviceAuthentication()
                    .fold({
                        close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "invalid authentication: $it"))
                    }, { deviceActor ->
                        logger.debug("currently connected devices: ${connections.keys}")
                        logger.debug("new connection $this for device $deviceActor")
                        removeExistingConnectionIfExists(deviceActor.deviceId)
                        logger.debug("adding connection for device $deviceActor")
                        connections[deviceActor.deviceId] = this
                        logger.debug("currently connected devices: ${connections.keys}")

                        send("connected to fabX")

                        try {
                            for (frame in incoming) {
                                logger.debug("received $frame from ${deviceActor.name}")

                                frame as? Frame.Text ?: continue
                                val text = frame.readText()
                                logger.debug("received \"$text\" from ${deviceActor.name}")

                                Unit.right()
                                    .flatMap {
                                        deserializeResponse(text)
                                            .map {
                                                responseChannels[it.commandId]?.send(it)
                                                    ?: logger.warn("Received response for unknown command (id ${it.commandId})")
                                            }
                                            .swap()
                                    }
                                    .flatMap {
                                        deserializeDeviceToServerNotification(text)
                                            .map { it.handle(deviceActor, notificationHandler) }
                                            .swap()
                                    }
                                    .flatMap {
                                        deserializeDeviceToServerCommand(text)
                                            .map { command ->
                                                command.handle(deviceActor, commandHandler)
                                                    .getOrHandle { errorHandler(command.commandId, it) }
                                                    .let {
                                                        val response = serializeResponse(it)
                                                        logger.debug("Sending response to ${deviceActor.deviceId}: $response")
                                                        send(response)
                                                    }
                                            }
                                            .swap()
                                    }
                                    .tap {
                                        logger.warn("Not able to deserialize incoming message from $deviceActor: $text")
                                    }
                            }
                        } catch (e: Exception) {
                            logger.warn("Exception during device websocket handling", e)
                        } finally {
                            logger.debug("Closed connection $this of ${deviceActor.name} for reason ${closeReason.await()}")

                            if (connections[deviceActor.deviceId] == this) {
                                connections.remove(deviceActor.deviceId)
                                logger.debug("Removed connection $this (is current connection of ${deviceActor.name})")
                            } else {
                                logger.debug(
                                    "Not removing connection, as $this is not current connection " +
                                            "of ${deviceActor.name} (${connections[deviceActor.deviceId]} is)"
                                )
                            }
                        }
                    })
            }
        }
    }

    private fun deserializeResponse(serialized: String): Either<Error, DeviceResponse> {
        return try {
            Json.decodeFromString<DeviceResponse>(serialized).right()
        } catch (e: SerializationException) {
            DeviceCommunicationSerializationError(e.localizedMessage).left()
        }
    }

    private fun deserializeDeviceToServerNotification(serialized: String): Either<Error, DeviceToServerNotification> {
        return try {
            Json.decodeFromString<DeviceToServerNotification>(serialized).right()
        } catch (e: SerializationException) {
            DeviceCommunicationSerializationError(e.localizedMessage).left()
        }
    }

    private fun deserializeDeviceToServerCommand(serialized: String): Either<Error, DeviceToServerCommand> {
        return try {
            Json.decodeFromString<DeviceToServerCommand>(serialized).right()
        } catch (e: SerializationException) {
            logger.warn(
                "SerializationException during device websocket handling (message: \"$serialized\")",
                e
            )
            DeviceCommunicationSerializationError(e.localizedMessage).left()
        }
    }

    private fun serializeResponse(response: DeviceResponse): String = Json.encodeToString(response)

    private fun serializeServerToDeviceCommand(command: ServerToDeviceCommand): String = Json.encodeToString(command)

    private fun errorHandler(commandId: Int, error: Error): DeviceResponse {
        return ErrorResponse(
            commandId,
            error.message,
            error.parameters,
            error.correlationId
        )
    }

    private fun removeExistingConnectionIfExists(deviceId: DeviceId) {
        connections.remove(deviceId)?.let {
            it.coroutineContext.cancel()
            logger.warn("Removed websocket to device $deviceId as new connection of same device was established.")
        } ?: run {
            logger.debug("No existing connection for $deviceId.")
        }
    }

    internal fun newCommandId(): Int = Random.nextInt()

    internal suspend fun sendCommand(
        deviceId: DeviceId,
        command: ServerToDeviceCommand,
        correlationId: CorrelationId
    ): Either<Error, Unit> {
        return connections[deviceId].toOption()
            .toEither {
                Error.DeviceNotConnected(
                    "Device with id $deviceId is currently not connected.",
                    deviceId,
                    correlationId
                )
            }
            .map {
                logger.debug("Sending command to $deviceId: $command")
                it.send(serializeServerToDeviceCommand(command))
            }
    }

    internal fun setupReceivingDeviceResponse(
        deviceId: DeviceId,
        commandId: Int,
        correlationId: CorrelationId
    ): Either<Error, Unit> {
        // directly return error if device is not even connected
        // - should not happen if sendCommand was called before (which already checks if device is connected)
        if (!connections.containsKey(deviceId)) {
            return Error.DeviceNotConnected(
                "Device with id $deviceId is currently not connected.",
                deviceId,
                correlationId
            ).left()
        }

        val channel = Channel<DeviceResponse>()
        responseChannels[commandId] = channel

        return Unit.right()
    }

    internal suspend fun receiveDeviceResponse(
        deviceId: DeviceId,
        commandId: Int,
        correlationId: CorrelationId
    ): Either<Error, DeviceResponse> {
        val channel = responseChannels[commandId]
            ?: throw IllegalStateException("Channel for receiving not found. Was setup called?")

        return withContext(Dispatchers.Default) {
            val result = async {
                val response = channel.receive()
                logger.debug("Received from channel: $response")

                response
            }

            // parallel job to cancel waiting job after timeout
            val timeoutJob = launch {
                delay(deviceReceiveTimeoutMillis)
                if (!result.isCompleted) {
                    result.cancel()
                }
            }

            try {
                val receivedResult = result.await()
                if (timeoutJob.isActive) {
                    timeoutJob.cancel()
                }
                receivedResult.right()
            } catch (e: CancellationException) {
                Error.DeviceTimeout(
                    "Timeout while waiting for response from device $deviceId.",
                    deviceId,
                    correlationId
                ).left()
            } finally {
                responseChannels.remove(commandId)
            }
        }
    }
}