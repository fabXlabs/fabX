package cloud.fabX.fabXaccess.tool.application

import arrow.core.Either
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.application.logger
import cloud.fabX.fabXaccess.common.model.Actor
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.ToolId
import cloud.fabX.fabXaccess.tool.model.Tool

/**
 * Service to get tools.
 */
class GettingTool {

    private val log = logger()
    private val toolRepository = DomainModule.toolRepository()

    fun getAll(
        actor: Actor,
        correlationId: CorrelationId
    ): Set<Tool> {
        log.debug("getAll (actor: $actor, correlationId: $correlationId)...")

        return toolRepository.getAll()
    }

    fun getById(
        actor: Actor,
        correlationId: CorrelationId,
        toolId: ToolId
    ): Either<Error, Tool> {
        log.debug("getById (actor: $actor, correlationId: $correlationId)...")

        return toolRepository.getById(toolId)
    }
}