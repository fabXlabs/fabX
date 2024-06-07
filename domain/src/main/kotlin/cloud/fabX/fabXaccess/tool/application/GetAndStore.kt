package cloud.fabX.fabXaccess.tool.application

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.right
import cloud.fabX.fabXaccess.common.model.Actor
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.model.ToolId
import cloud.fabX.fabXaccess.tool.model.Tool
import cloud.fabX.fabXaccess.tool.model.ToolRepository
import cloud.fabX.fabXaccess.tool.model.ToolSourcingEvent

suspend inline fun ToolRepository.getAndStoreMap(
    toolId: ToolId,
    actor: Actor,
    correlationId: CorrelationId,
    log: Logger,
    functionName: String,
    domainFunction: (Tool) -> ToolSourcingEvent
): Either<Error, Unit> {
    return this.getAndStoreFlatMap(toolId, actor, correlationId, log, functionName) {
        domainFunction(it).right()
    }
}

suspend inline fun ToolRepository.getAndStoreFlatMap(
    toolId: ToolId,
    actor: Actor,
    correlationId: CorrelationId,
    log: Logger,
    functionName: String,
    domainFunction: (Tool) -> Either<Error, ToolSourcingEvent>
): Either<Error, Unit> {
    return log.logError(actor, correlationId, functionName) {
        getById(toolId)
            .flatMap { domainFunction(it) }
            .flatMap { store(it) }
    }
}

suspend inline fun <R> Logger.logError(
    actor: Actor,
    correlationId: CorrelationId,
    functionName: String,
    function: () -> Either<Error, R>
): Either<Error, R> {
    this.debug("$functionName (actor: $actor, correlationId: $correlationId)...")
    return function()
        .onRight { this.debug("...$functionName done") }
        .onLeft { this.error("...$functionName error: $it") }
}