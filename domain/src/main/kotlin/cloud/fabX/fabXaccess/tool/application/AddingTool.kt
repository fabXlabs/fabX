package cloud.fabX.fabXaccess.tool.application

import arrow.core.Option
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.application.logger
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.QualificationId
import cloud.fabX.fabXaccess.tool.model.IdleState
import cloud.fabX.fabXaccess.tool.model.Tool
import cloud.fabX.fabXaccess.tool.model.ToolType
import cloud.fabX.fabXaccess.user.model.Admin

/**
 * Service to add new tools.
 */
class AddingTool {

    private val log = logger()
    private val toolRepository = DomainModule.toolRepository()

    fun addTool(
        actor: Admin,
        correlationId: CorrelationId,
        name: String,
        type: ToolType,
        time: Int,
        idleState: IdleState,
        wikiLink: String,
        requiredQualifications: Set<QualificationId>
    ): Option<Error> {
        log.debug("addTool...")

        return toolRepository
            .store(
                Tool.addNew(
                    actor,
                    correlationId,
                    name,
                    type,
                    time,
                    idleState,
                    wikiLink,
                    requiredQualifications
                )
            )
            .tapNone { log.debug("...addTool done") }
            .tap { log.debug("...addTool error: $it") }
    }
}