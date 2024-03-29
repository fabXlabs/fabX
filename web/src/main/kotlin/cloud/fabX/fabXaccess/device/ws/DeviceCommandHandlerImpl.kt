package cloud.fabX.fabXaccess.device.ws

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.toOption
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.newCorrelationId
import cloud.fabX.fabXaccess.device.application.GettingConfiguration
import cloud.fabX.fabXaccess.device.model.DeviceActor
import cloud.fabX.fabXaccess.tool.rest.toRestModel
import cloud.fabX.fabXaccess.user.application.GettingAuthorizedTools
import cloud.fabX.fabXaccess.user.application.ValidatingSecondFactor
import cloud.fabX.fabXaccess.user.model.PinIdentity
import cloud.fabX.fabXaccess.user.rest.AuthenticationService

class DeviceCommandHandlerImpl(
    private val gettingConfiguration: GettingConfiguration,
    private val gettingAuthorizedTools: GettingAuthorizedTools,
    private val validatingSecondFactor: ValidatingSecondFactor,
    private val authenticationService: AuthenticationService
) : DeviceCommandHandler {
    override suspend fun handle(actor: DeviceActor, command: GetConfiguration): Either<Error, DeviceResponse> {
        return gettingConfiguration.getConfiguration(
            actor,
            newCorrelationId(),
            command.actualFirmwareVersion
        ).map { configuration ->
            ConfigurationResponse(
                command.commandId,
                configuration.device.name,
                configuration.device.background,
                configuration.device.backupBackendUrl,
                configuration.attachedTools.mapValues {
                    ToolConfigurationResponse(
                        it.value.id.serialize(),
                        it.value.name,
                        it.value.type.toRestModel(),
                        it.value.requires2FA,
                        it.value.time,
                        it.value.idleState.toRestModel()
                    )
                }
            )
        }
    }

    override suspend fun handle(actor: DeviceActor, command: GetAuthorizedTools): Either<Error, DeviceResponse> {
        val correlationId = newCorrelationId()

        return authenticationService
            .augmentDeviceActorOnBehalfOfUser(
                actor,
                command.cardIdentity,
                command.phoneNrIdentity,
                correlationId
            )
            .flatMap {
                gettingAuthorizedTools.getAuthorizedTools(it, correlationId)
            }
            .map {
                AuthorizedToolsResponse(
                    command.commandId,
                    it.map { tool -> tool.id.serialize() }.toSet()
                )
            }
    }

    override suspend fun handle(actor: DeviceActor, command: ValidateSecondFactor): Either<Error, DeviceResponse> {
        val correlationId = newCorrelationId()

        return authenticationService
            .augmentDeviceActorOnBehalfOfUser(
                actor,
                command.cardIdentity,
                command.phoneNrIdentity,
                correlationId
            )
            .flatMap { augmentedDeviceActor ->
                command.pinSecondIdentity.toOption().toEither {
                    Error.InvalidSecondFactor("Second Factor not provided.", correlationId)
                }.flatMap {
                    validatingSecondFactor.validateSecondFactor(
                        augmentedDeviceActor,
                        correlationId,
                        PinIdentity(it.pin)
                    )
                }
            }
            .map {
                ValidSecondFactorResponse(command.commandId)
            }
    }
}