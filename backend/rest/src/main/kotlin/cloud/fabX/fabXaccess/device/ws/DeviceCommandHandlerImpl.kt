package cloud.fabX.fabXaccess.device.ws

import arrow.core.Either
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.device.application.GettingConfiguration
import cloud.fabX.fabXaccess.device.model.DeviceActor
import cloud.fabX.fabXaccess.tool.rest.toRestModel

class DeviceCommandHandlerImpl(
    private val gettingConfiguration: GettingConfiguration
) : DeviceCommandHandler {
    override suspend fun handle(actor: DeviceActor, command: GetConfiguration): Either<Error, DeviceResponse> {
        return gettingConfiguration.getConfiguration(actor).map { configuration ->
            ConfigurationResponse(
                command.commandId,
                configuration.device.name,
                configuration.device.background,
                configuration.device.backupBackendUrl,
                configuration.attachedTools.mapValues {
                    ToolConfigurationResponse(
                        it.value.name,
                        it.value.type.toRestModel(),
                        it.value.time,
                        it.value.idleState.toRestModel()
                    )
                }
            )
        }
    }
}