package cloud.fabX.fabXaccess.tool.application

import arrow.core.Either
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.Actor
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.ToolId
import cloud.fabX.fabXaccess.tool.model.Tool
import cloud.fabX.fabXaccess.tool.model.ToolRepository

/**
 * Service to get tools.
 */
class GettingTool(
    loggerFactory: LoggerFactory,
    private val toolRepository: ToolRepository
) {
    private val log = loggerFactory.invoke(this::class.java)

    suspend fun getAll(
        actor: Actor,
        correlationId: CorrelationId
    ): Set<Tool> {
        log.debug("getAll (actor: $actor, correlationId: $correlationId)...")

        return toolRepository.getAll()
    }

    suspend fun getById(
        actor: Actor,
        correlationId: CorrelationId,
        toolId: ToolId
    ): Either<Error, Tool> {
        log.debug("getById (actor: $actor, correlationId: $correlationId)...")

        return toolRepository.getById(toolId)
    }
}