package cloud.fabX.fabXaccess.device.ws

import arrow.core.Either
import arrow.core.getOrHandle
import arrow.core.left
import arrow.core.right
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.DeviceId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.Error.SerializationError
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
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// TODO unit test, integration test
class DeviceWebsocketController(
    loggerFactory: LoggerFactory,
    private val commandHandler: DeviceCommandHandler
) {
    private val logger = loggerFactory.invoke(this::class.java)

    private val connections: MutableMap<DeviceId, DefaultWebSocketSession> =
        Collections.synchronizedMap(HashMap())

    val routes: Route.() -> Unit = {
        route("/device") {
            webSocket("ws") {
                readDeviceAuthentication()
                    .fold({
                        close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "invalid authentication: $it"))
                    }, { device ->
                        logger.debug("new connection $this for device $device")
                        closeExistingConnectionIfExists(device.id)
                        connections[device.id] = this

                        send("connected to fabX ${Json.encodeToString<DeviceCommand>(GetConfiguration(123))}")

                        try {
                            for (frame in incoming) {
                                frame as? Frame.Text ?: continue
                                val text = frame.readText()
                                logger.debug("received \"$text\" from ${device.name}")

                                deserializeCommand(text)
                                    .map { command ->
                                        command.handle(device.asActor(), commandHandler)
                                            .getOrHandle { errorHandler(command.commandId, it) }
                                    }
                                    .getOrHandle { errorHandler(-1, it) }
                                    .let { send(serializeResponse(it)) }
                            }
                        } catch (e: Exception) {
                            logger.warn("Exception during device websocket handling", e)
                        } finally {
                            logger.debug("Closed connection $this of ${device.name}")
                            connections.remove(device.id)
                        }
                    })
            }
        }
    }

    private fun deserializeCommand(serialized: String): Either<Error, DeviceCommand> {
        return try {
            Json.decodeFromString<DeviceCommand>(serialized).right()
        } catch (e: SerializationException) {
            logger.warn(
                "SerializationException during device websocket handling (message: \"$serialized\")",
                e
            )
            SerializationError(e.localizedMessage).left()
        }
    }

    private fun serializeResponse(response: DeviceResponse): String = Json.encodeToString(response)

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
}