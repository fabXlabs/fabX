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


// TODO - commands device -> server (and corresponding responses)
//          - done: get configuration
//          - done: get which tools user (with authentication xyz) is allowed to use
//      - notification device -> server
//          - tool x was just unlocked
//      - commands server -> device (response?)
//          - unlock tool
//          - restart now
//          - (restart now for firmware update)


/**
 * A command requiring a response. Can be sent from device to server or the other way around.
 */
@Serializable
sealed class DeviceCommand {
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
data class GetConfiguration(override val commandId: Long) : DeviceCommand() {
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
 * Command from device -> server. In the response, the server returns which tools the authenticated user is authorized
 * to use.
 */
@Serializable
data class GetAuthorizedTools(
    override val commandId: Long,
    val phoneNrIdentity: PhoneNrIdentity?,
    val cardIdentity: CardIdentity?
) : DeviceCommand() {

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