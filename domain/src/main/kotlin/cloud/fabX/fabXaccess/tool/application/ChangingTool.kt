package cloud.fabX.fabXaccess.tool.application

import arrow.core.Option
import arrow.core.flatMap
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.QualificationId
import cloud.fabX.fabXaccess.common.model.ToolId
import cloud.fabX.fabXaccess.qualification.model.GettingQualificationById
import cloud.fabX.fabXaccess.tool.model.IdleState
import cloud.fabX.fabXaccess.tool.model.ToolRepository
import cloud.fabX.fabXaccess.tool.model.ToolType
import cloud.fabX.fabXaccess.user.model.Admin
import kotlinx.datetime.Clock

/**
 * Service to handle changing tool properties.
 */
class ChangingTool(
    loggerFactory: LoggerFactory,
    private val toolRepository: ToolRepository,
    private val gettingQualificationById: GettingQualificationById,
    private val clock: Clock
) {
    private val log = loggerFactory.invoke(this::class.java)

    suspend fun changeToolDetails(
        actor: Admin,
        correlationId: CorrelationId,
        toolId: ToolId,
        name: ChangeableValue<String>,
        type: ChangeableValue<ToolType>,
        requires2FA: ChangeableValue<Boolean>,
        time: ChangeableValue<Int>,
        idleState: ChangeableValue<IdleState>,
        enabled: ChangeableValue<Boolean>,
        wikiLink: ChangeableValue<String>,
        requiredQualifications: ChangeableValue<Set<QualificationId>>
    ): Option<Error> {
        log.debug("changeToolDetails...")

        return toolRepository.getById(toolId)
            .flatMap {
                it.changeDetails(
                    actor,
                    clock,
                    correlationId,
                    name,
                    type,
                    requires2FA,
                    time,
                    idleState,
                    enabled,
                    wikiLink,
                    requiredQualifications,
                    gettingQualificationById
                )
            }
            .flatMap {
                toolRepository.store(it)
                    .toEither { }
                    .swap()
            }
            .swap()
            .orNone()
            .tapNone { log.debug("...changeToolDetails done") }
            .tap { log.error("...changeToolDetails error: $it") }
    }
}