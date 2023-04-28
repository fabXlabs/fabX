package cloud.fabX.fabXaccess.device.ws

import arrow.core.Either
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.device.model.DeviceActor
import cloud.fabX.fabXaccess.tool.rest.IdleState
import cloud.fabX.fabXaccess.tool.rest.ToolType
import cloud.fabX.fabXaccess.user.rest.CardIdentity
import cloud.fabX.fabXaccess.user.rest.PhoneNrIdentity
import cloud.fabX.fabXaccess.user.rest.PinIdentityDetails
import kotlinx.serialization.Serializable

/**
 * A command requiring a response. Can be sent from device to server or the other way around.
 */
@Serializable
sealed class DeviceToServerCommand {
    abstract val commandId: Int

    abstract suspend fun handle(
        actor: DeviceActor,
        commandHandler: DeviceCommandHandler
    ): Either<Error, DeviceResponse>
}

interface DeviceCommandHandler {
    suspend fun handle(actor: DeviceActor, command: GetConfiguration): Either<Error, DeviceResponse>
    suspend fun handle(actor: DeviceActor, command: GetAuthorizedTools): Either<Error, DeviceResponse>
    suspend fun handle(actor: DeviceActor, command: ValidateSecondFactor): Either<Error, DeviceResponse>
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
    abstract val commandId: Int
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
    abstract val commandId: Int
}

/**
 * Generic error response.
 */
@Serializable
data class ErrorResponse(
    override val commandId: Int,
    val message: String,
    val parameters: Map<String, String>,
    val correlationId: CorrelationId?
) : DeviceResponse()

/**
 * Command from device -> server. In the response, the server returns the device's configuration.
 */
@Serializable
data class GetConfiguration(
    override val commandId: Int,
    val actualFirmwareVersion: String
) : DeviceToServerCommand() {
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
    override val commandId: Int,
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
    val id: String,
    val name: String,
    val type: ToolType,
    val requires2FA: Boolean,
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
    override val commandId: Int,
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
    override val commandId: Int,
    val toolIds: Set<String>
) : DeviceResponse()

/**
 * Command from device -> service. If the server returns [ValidSecondFactorResponse], the second factor is valid.
 * Otherwise, the server returns [ErrorResponse].
 */
@Serializable
data class ValidateSecondFactor(
    override val commandId: Int,
    // primary factor
    val phoneNrIdentity: PhoneNrIdentity?,
    val cardIdentity: CardIdentity?,
    // second factor
    val pinSecondIdentity: PinIdentityDetails?
) : DeviceToServerCommand() {

    override suspend fun handle(
        actor: DeviceActor,
        commandHandler: DeviceCommandHandler
    ): Either<Error, DeviceResponse> = commandHandler.handle(actor, this)
}

@Serializable
data class ValidSecondFactorResponse(override val commandId: Int) : DeviceResponse()

/**
 * Command from server -> device to unlock a tool given by the tool id.
 */
@Serializable
data class UnlockTool(
    override val commandId: Int,
    val toolId: String
) : ServerToDeviceCommand()

@Serializable
data class ToolUnlockResponse(override val commandId: Int) : DeviceResponse()

/**
 * Command from server -> device to restart the device.
 */
@Serializable
data class RestartDevice(override val commandId: Int) : ServerToDeviceCommand()

@Serializable
data class DeviceRestartResponse(override val commandId: Int) : DeviceResponse()

/**
 * Command from server -> device to trigger device firmware update process.
 */
@Serializable
data class UpdateDeviceFirmware(override val commandId: Int) : ServerToDeviceCommand()

@Serializable
data class UpdateFirmwareResponse(override val commandId: Int) : DeviceResponse()
