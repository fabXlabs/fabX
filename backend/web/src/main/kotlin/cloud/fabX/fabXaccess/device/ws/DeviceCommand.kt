package cloud.fabX.fabXaccess.device.ws

import arrow.core.Either
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.device.model.DeviceActor
import cloud.fabX.fabXaccess.tool.rest.IdleState
import cloud.fabX.fabXaccess.tool.rest.ToolType
import cloud.fabX.fabXaccess.user.rest.CardIdentity
import cloud.fabX.fabXaccess.user.rest.PhoneNrIdentity
import kotlinx.serialization.Serializable

/**
 * A command requiring a response. Can be sent from device to server or the other way around.
 */
@Serializable
sealed class DeviceToServerCommand {
    abstract val commandId: Long

    abstract suspend fun handle(
        actor: DeviceActor,
        commandHandler: DeviceCommandHandler
    ): Either<Error, DeviceResponse>
}

interface DeviceCommandHandler {
    suspend fun handle(actor: DeviceActor, command: GetConfiguration): Either<Error, DeviceResponse>
    suspend fun handle(actor: DeviceActor, command: GetAuthorizedTools): Either<Error, DeviceResponse>
}

@Serializable
sealed class DeviceToServerNotification {
    abstract suspend fun handle(
        actor: DeviceActor,
        deviceNotificationHandler: DeviceNotificationHandler
    ): Either<Error, Unit>
}

interface DeviceNotificationHandler {
    suspend fun handle(actor: DeviceActor, notification: ToolUnlockedNotification): Either<Error, Unit>
}

@Serializable
sealed class ServerToDeviceCommand {
    abstract val commandId: Long
}

/**
 * A response to a command.
 *
 */
@Serializable
sealed class DeviceResponse {
    /**
     * Contains the commandId of the request eliciting this response.
     */
    abstract val commandId: Long
}

/**
 * Generic error response.
 */
@Serializable
data class ErrorResponse(
    override val commandId: Long,
    val message: String,
    val parameters: Map<String, String>,
    val correlationId: CorrelationId?
) : DeviceResponse()

/**
 * Command from device -> server. In the response, the server returns the device's configuration.
 */
@Serializable
data class GetConfiguration(override val commandId: Long) : DeviceToServerCommand() {
    override suspend fun handle(
        actor: DeviceActor,
        commandHandler: DeviceCommandHandler
    ): Either<Error, DeviceResponse> =
        commandHandler.handle(actor, this)
}

/**
 * Response from server -> device containing a device's configuration.
 */
@Serializable
data class ConfigurationResponse(
    override val commandId: Long,
    val name: String,
    val background: String,
    val backupBackendUrl: String,
    val attachedTools: Map<Int, ToolConfigurationResponse>
) : DeviceResponse()

/**
 * Part of [ConfigurationResponse] containing configuration of an attached tool.
 */
@Serializable
data class ToolConfigurationResponse(
    val name: String,
    val type: ToolType,
    val time: Int,
    val idleState: IdleState
)

/**
 * Notification from device -> server sent when a user has selected a tool at a device and the tool was thus unlocked.
 */
@Serializable
data class ToolUnlockedNotification(
    val toolId: String,
    val phoneNrIdentity: PhoneNrIdentity?,
    val cardIdentity: CardIdentity?
) : DeviceToServerNotification() {
    override suspend fun handle(
        actor: DeviceActor,
        deviceNotificationHandler: DeviceNotificationHandler
    ): Either<Error, Unit> =
        deviceNotificationHandler.handle(actor, this)
}

/**
 * Command from device -> server. In the response, the server returns which tools the authenticated user is authorized
 * to use.
 */
@Serializable
data class GetAuthorizedTools(
    override val commandId: Long,
    val phoneNrIdentity: PhoneNrIdentity?,
    val cardIdentity: CardIdentity?
) : DeviceToServerCommand() {

    override suspend fun handle(
        actor: DeviceActor,
        commandHandler: DeviceCommandHandler
    ): Either<Error, DeviceResponse> = commandHandler.handle(actor, this)
}

@Serializable
data class AuthorizedToolsResponse(
    override val commandId: Long,
    val toolIds: Set<String>
) : DeviceResponse()

/**
 * Command from server -> device to unlock a tool given by the tool id.
 */
@Serializable
data class UnlockTool(
    override val commandId: Long,
    val toolId: String
) : ServerToDeviceCommand()

@Serializable
data class ToolUnlockResponse(override val commandId: Long) : DeviceResponse()