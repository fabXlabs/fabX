package cloud.fabX.fabXaccess.tool.application

import arrow.core.Option
import arrow.core.flatMap
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.DomainEventPublisher
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.ToolDeleted
import cloud.fabX.fabXaccess.common.model.ToolId
import cloud.fabX.fabXaccess.tool.model.ToolRepository
import cloud.fabX.fabXaccess.user.model.Admin
import kotlinx.datetime.Clock

/**
 * Service to handle deleting a tool.
 */
class DeletingTool(
    loggerFactory: LoggerFactory,
    private val clock: Clock,
    private val domainEventPublisher: DomainEventPublisher,
    private val toolRepository: ToolRepository
) {
    private val log = loggerFactory.invoke(this::class.java)

    suspend fun deleteTool(
        actor: Admin,
        correlationId: CorrelationId,
        toolId: ToolId
    ): Option<Error> {
        log.debug("deleteTool...")

        return toolRepository.getById(toolId)
            .map {
                it.delete(actor, clock, correlationId)
            }
            .flatMap {
                toolRepository.store(it)
                    .toEither { }
                    .swap()
            }
            .swap()
            .getOrNone()
            .onNone { log.debug("...deleteTool done") }
            .onNone {
                log.debug("publishing ToolDeleted event...")
                domainEventPublisher.publish(
                    ToolDeleted(
                        actor.id,
                        clock.now(),
                        correlationId,
                        toolId
                    )
                )
            }
            .onSome { log.error("...deleteTool error: $it") }
    }
}