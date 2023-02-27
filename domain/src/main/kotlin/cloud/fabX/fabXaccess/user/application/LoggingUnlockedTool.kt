package cloud.fabX.fabXaccess.user.application

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.toOption
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.model.TaggedCounter
import cloud.fabX.fabXaccess.common.model.ToolId
import cloud.fabX.fabXaccess.device.model.DeviceActor
import cloud.fabX.fabXaccess.tool.model.ToolRepository

/**
 * Service to log that a user has unlocked a certain tool.
 */
class LoggingUnlockedTool(
    private val toolUsageCounter: TaggedCounter<ToolId>,
    private val toolRepository: ToolRepository,
    loggerFactory: LoggerFactory
) {
    private val log: Logger = loggerFactory.invoke(this::class.java)

    suspend fun logUnlockedTool(
        actor: DeviceActor,
        toolId: ToolId,
        correlationId: CorrelationId
    ): Either<Error, Unit> {
        return actor.onBehalfOf.toOption()
            .toEither { Error.NotAuthenticated("Required authentication not found.", correlationId) }
            .map { it.userId }
            .onRight {
                toolUsageCounter.increment(toolId, {
                    toolRepository.getById(toolId)
                        .map { mapOf("toolId" to it.id.serialize(), "toolName" to it.name) }
                        .getOrElse {
                            log.error("Not able to get tool for $toolId")
                            mapOf("toolId" to toolId.serialize())
                        }
                })
            }
            .onRight {
                log.info("User $it unlocked tool $toolId")
            }
            .onLeft { log.error("Error while logging tool unlock ($actor, $toolId): $it") }
            .map { }
    }
}