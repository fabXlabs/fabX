package cloud.fabX.fabXaccess.tool.application

import arrow.core.Either
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.QualificationId
import cloud.fabX.fabXaccess.common.model.ToolId
import cloud.fabX.fabXaccess.common.model.ToolIdFactory
import cloud.fabX.fabXaccess.tool.model.IdleState
import cloud.fabX.fabXaccess.tool.model.Tool
import cloud.fabX.fabXaccess.tool.model.ToolRepository
import cloud.fabX.fabXaccess.tool.model.ToolType
import cloud.fabX.fabXaccess.user.model.Admin

/**
 * Service to add new tools.
 */
class AddingTool(
    loggerFactory: LoggerFactory,
    private val toolRepository: ToolRepository,
    private val toolIdFactory: ToolIdFactory
) {
    private val log = loggerFactory.invoke(this::class.java)

    fun addTool(
        actor: Admin,
        correlationId: CorrelationId,
        name: String,
        type: ToolType,
        time: Int,
        idleState: IdleState,
        wikiLink: String,
        requiredQualifications: Set<QualificationId>
    ): Either<Error, ToolId> {
        log.debug("addTool...")

        val sourcingEvent = Tool.addNew(
            toolIdFactory,
            actor,
            correlationId,
            name,
            type,
            time,
            idleState,
            wikiLink,
            requiredQualifications
        )

        return toolRepository
            .store(sourcingEvent)
            .toEither { }
            .swap()
            .map { sourcingEvent.aggregateRootId }
            .tap { log.debug("...addTool done") }
            .tapLeft { log.debug("...addTool error: $it") }
    }
}