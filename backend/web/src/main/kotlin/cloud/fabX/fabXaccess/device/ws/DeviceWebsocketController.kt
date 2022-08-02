package cloud.fabX.fabXaccess.device.ws

import arrow.core.Either
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
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.DefaultWebSocketSession
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.close
import io.ktor.http.cio.websocket.readText
import io.ktor.http.cio.websocket.send
import io.ktor.routing.Route
import io.ktor.routing.route
import io.ktor.websocket.webSocket
import java.util.Collections
import kotlin.random.Random
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class DeviceWebsocketController(
    loggerFactory: LoggerFactory,
    private val commandHandler: DeviceCommandHandler
) {
    private val logger = loggerFactory.invoke(this::class.java)

    private val connections: MutableMap<DeviceId, DefaultWebSocketSession> =
        Collections.synchronizedMap(HashMap())

    private val responseChannels: MutableMap<Long, SendChannel<DeviceResponse>> =
        Collections.synchronizedMap(HashMap())

    val routes: Route.() -> Unit = {
        route("/device") {
            webSocket("ws") {
                readDeviceAuthentication()
                    .fold({
                        close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "invalid authentication: $it"))
                    }, { deviceActor ->
                        logger.debug("new connection $this for device $deviceActor")
                        closeExistingConnectionIfExists(deviceActor.deviceId)
                        connections[deviceActor.deviceId] = this

                        send("connected to fabX")

                        try {
                            for (frame in incoming) {
                                frame as? Frame.Text ?: continue
                                val text = frame.readText()
                                logger.debug("received \"$text\" from ${deviceActor.name}")

                                deserializeResponse(text)
                                    .map {
                                        responseChannels.remove(it.commandId)?.send(it)
                                            ?: logger.warn("Received response for unknown command (id ${it.commandId})")
                                    }
                                    .fold({
                                        deserializeDeviceToServerCommand(text)
                                            .map { command ->
                                                command.handle(deviceActor, commandHandler)
                                                    .getOrHandle { errorHandler(command.commandId, it) }
                                            }
                                            .getOrHandle { errorHandler(-1, it) }
                                            .let { send(serializeResponse(it)) }
                                    }, {})
                            }
                        } catch (e: Exception) {
                            logger.warn("Exception during device websocket handling", e)
                        } finally {
                            logger.debug("Closed connection $this of ${deviceActor.name}")
                            connections.remove(deviceActor.deviceId)
                        }
                    })
            }
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

    private fun deserializeResponse(serialized: String): Either<Error, DeviceResponse> {
        return try {
            Json.decodeFromString<DeviceResponse>(serialized).right()
        } catch (e: SerializationException) {
            DeviceCommunicationSerializationError(e.localizedMessage).left()
        }
    }

    private fun serializeResponse(response: DeviceResponse): String = Json.encodeToString(response)

    private fun serializeServerToDeviceCommand(command: ServerToDeviceCommand): String = Json.encodeToString(command)

    private fun errorHandler(commandId: Long, error: Error): DeviceResponse {
        return ErrorResponse(
            commandId,
            error.message,
            error.parameters,
            error.correlationId
        )
    }

    private suspend fun closeExistingConnectionIfExists(deviceId: DeviceId) {
        connections.remove(deviceId)?.let {
            it.close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "New connection of same device established"))
            logger.warn("Closed websocket to device $deviceId as new connection of same device was established.")
        }
    }

    internal fun newCommandId(): Long = Random.nextLong()

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
                it.send(serializeServerToDeviceCommand(command))
            }
    }

    internal suspend fun receiveDeviceResponse(
        deviceId: DeviceId,
        commandId: Long,
        correlationId: CorrelationId
    ): Either<Error, DeviceResponse> {
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

        return withContext(Dispatchers.Default) {
            val result = async {
                val response = channel.receive()
                logger.debug("Received from channel: $response")

                response
            }

            // parallel job to cancel waiting job after timeout
            val timeoutJob = launch {
                // TODO make timeout configurable
                delay(5000)
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
            }
        }
    }
}