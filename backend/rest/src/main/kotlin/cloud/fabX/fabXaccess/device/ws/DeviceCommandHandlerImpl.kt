package cloud.fabX.fabXaccess.device.ws

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.newCorrelationId
import cloud.fabX.fabXaccess.device.application.GettingConfiguration
import cloud.fabX.fabXaccess.device.model.DeviceActor
import cloud.fabX.fabXaccess.tool.rest.toRestModel
import cloud.fabX.fabXaccess.user.application.GettingAuthorizedTools
import cloud.fabX.fabXaccess.user.model.CardIdentity
import cloud.fabX.fabXaccess.user.model.GettingUserByIdentity
import cloud.fabX.fabXaccess.user.model.PhoneNrIdentity

class DeviceCommandHandlerImpl(
    private val gettingConfiguration: GettingConfiguration,
    private val gettingUserByIdentity: GettingUserByIdentity,
    private val gettingAuthorizedTools: GettingAuthorizedTools
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

    override suspend fun handle(actor: DeviceActor, command: GetAuthorizedTools): Either<Error, DeviceResponse> {
        val correlationId = newCorrelationId()

        val cardUser = command.cardIdentity?.let {
            CardIdentity.fromUnvalidated(it.cardId, it.cardSecret, correlationId)
                .flatMap { identity -> gettingUserByIdentity.getByIdentity(identity) }
                .fold({ error ->
                    return error.left()
                }, { user ->
                    user
                })
        }

        val phoneNrUser = command.phoneNrIdentity?.let {
            PhoneNrIdentity.fromUnvalidated(it.phoneNr, correlationId)
                .flatMap { identity -> gettingUserByIdentity.getByIdentity(identity) }
                .fold({ error ->
                    return error.left()
                }, { user ->
                    user
                })
        }

        // assert both identities authenticate the same user
        cardUser?.let { cu ->
            phoneNrUser?.let { pu ->
                if (cu.id != pu.id) {
                    return Error.NotAuthenticated("Required authentication not found.").left()
                }
            }
        }

        val actorOnBehalfOf = phoneNrUser?.let { actor.copy(onBehalfOf = it.asMember()) }
            ?: cardUser?.let { actor.copy(onBehalfOf = it.asMember()) }
            ?: actor

        return gettingAuthorizedTools.getAuthorizedTools(actorOnBehalfOf, correlationId)
            .map {
                AuthorizedToolsResponse(
                    command.commandId,
                    it.map { tool -> tool.id.serialize() }.toSet()
                )
            }
    }
}