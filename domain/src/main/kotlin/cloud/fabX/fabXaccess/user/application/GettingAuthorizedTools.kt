package cloud.fabX.fabXaccess.user.application

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.raise.either
import arrow.core.toOption
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.device.model.DeviceActor
import cloud.fabX.fabXaccess.device.model.DeviceRepository
import cloud.fabX.fabXaccess.tool.application.logError
import cloud.fabX.fabXaccess.tool.model.Tool
import cloud.fabX.fabXaccess.tool.model.ToolRepository

/**
 * Service to get which tools the authenticated user is authorized to use at the acting device.
 */
class GettingAuthorizedTools(
    loggerFactory: LoggerFactory,
    private val deviceRepository: DeviceRepository,
    private val toolRepository: ToolRepository
) {
    private val log: Logger = loggerFactory.invoke(this::class.java)

    suspend fun getAuthorizedTools(
        actor: DeviceActor,
        correlationId: CorrelationId
    ): Either<Error, Set<Tool>> =
        log.logError(actor, correlationId, "getAuthorizedTools") {
            actor.onBehalfOf.toOption()
                .toEither { Error.NotAuthenticated("Required authentication not found.", correlationId) }
                .map { it.qualifications }
                .flatMap { memberQualifications ->
                    deviceRepository.getById(actor.deviceId)
                        .map { it.attachedTools.values }
                        .flatMap { attachedToolIds ->
                            attachedToolIds.map { toolId -> toolRepository.getById(toolId) }
                                .let { eithers -> either { eithers.bindAll() } }
                                .map { tools ->
                                    tools.filter { it.enabled }
                                        .filter { memberQualifications.containsAll(it.requiredQualifications) }
                                        .toSet()
                                }
                        }
                }
        }
}