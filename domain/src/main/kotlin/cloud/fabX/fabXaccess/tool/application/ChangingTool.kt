package cloud.fabX.fabXaccess.tool.application

import arrow.core.Option
import arrow.core.flatMap
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.application.logger
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.qualification.model.QualificationId
import cloud.fabX.fabXaccess.tool.model.IdleState
import cloud.fabX.fabXaccess.tool.model.ToolId
import cloud.fabX.fabXaccess.tool.model.ToolType
import cloud.fabX.fabXaccess.user.model.Admin

/**
 * Service to handle changing tool properties.
 */
class ChangingTool {

    private val log = logger()
    private val toolRepository = DomainModule.toolRepository()

    fun changeToolDetails(
        actor: Admin,
        toolId: ToolId,
        name: ChangeableValue<String>,
        type: ChangeableValue<ToolType>,
        time: ChangeableValue<Int>,
        idleState: ChangeableValue<IdleState>,
        enabled: ChangeableValue<Boolean>,
        wikiLink: ChangeableValue<String>,
        requiredQualifications: ChangeableValue<Set<QualificationId>>
    ): Option<Error> {
        log.debug("changeToolDetails...")

        return toolRepository.getById(toolId)
            .map {
                it.changeDetails(
                    actor,
                    name,
                    type,
                    time,
                    idleState,
                    enabled,
                    wikiLink,
                    requiredQualifications
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