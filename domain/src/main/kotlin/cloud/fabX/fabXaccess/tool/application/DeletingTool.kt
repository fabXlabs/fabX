package cloud.fabX.fabXaccess.tool.application

import arrow.core.Option
import arrow.core.flatMap
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.application.logger
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.tool.model.ToolId
import cloud.fabX.fabXaccess.user.model.Admin

/**
 * Service to handle deleting a tool.
 */
class DeletingTool {

    private val log = logger()
    private val toolRepository = DomainModule.toolRepository()

    fun deleteTool(
        actor: Admin,
        toolId: ToolId
    ): Option<Error> {
        log.debug("deleteTool...")

        return toolRepository.getById(toolId)
            .map {
                it.delete(actor)
            }
            .flatMap {
                toolRepository.store(it)
                    .toEither { }
                    .swap()
            }
            .swap()
            .orNone()
            .tapNone { log.debug("...deleteTool done") }
            .tap { log.error("...deleteTool error: $it") }
    }
}