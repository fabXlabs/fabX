package cloud.fabX.fabXaccess.tool.application

import arrow.core.Either
import arrow.core.flatMap
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.QualificationId
import cloud.fabX.fabXaccess.common.model.ToolId
import cloud.fabX.fabXaccess.common.model.ToolIdFactory
import cloud.fabX.fabXaccess.qualification.model.GettingQualificationById
import cloud.fabX.fabXaccess.tool.model.IdleState
import cloud.fabX.fabXaccess.tool.model.Tool
import cloud.fabX.fabXaccess.tool.model.ToolRepository
import cloud.fabX.fabXaccess.tool.model.ToolType
import cloud.fabX.fabXaccess.user.model.Admin
import kotlinx.datetime.Clock

/**
 * Service to add new tools.
 */
class AddingTool(
    loggerFactory: LoggerFactory,
    private val toolRepository: ToolRepository,
    private val toolIdFactory: ToolIdFactory,
    private val gettingQualificationById: GettingQualificationById,
    private val clock: Clock
) {
    private val log = loggerFactory.invoke(this::class.java)

    suspend fun addTool(
        actor: Admin,
        correlationId: CorrelationId,
        name: String,
        type: ToolType,
        requires2FA: Boolean,
        time: Int,
        idleState: IdleState,
        wikiLink: String,
        requiredQualifications: Set<QualificationId>
    ): Either<Error, ToolId> {
        log.debug("addTool...")

        return Tool
            .addNew(
                toolIdFactory,
                actor,
                clock,
                correlationId,
                name,
                type,
                requires2FA,
                time,
                idleState,
                wikiLink,
                requiredQualifications,
                gettingQualificationById
            )
            .flatMap {
                toolRepository.store(it)
                    .toEither { it.aggregateRootId }
                    .swap()
            }
            .tap { log.debug("...addTool done") }
            .tapLeft { log.debug("...addTool error: $it") }
    }
}