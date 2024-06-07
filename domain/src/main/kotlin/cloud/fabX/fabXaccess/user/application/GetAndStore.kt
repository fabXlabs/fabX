package cloud.fabX.fabXaccess.user.application

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.right
import cloud.fabX.fabXaccess.common.model.Actor
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.user.model.User
import cloud.fabX.fabXaccess.user.model.UserRepository
import cloud.fabX.fabXaccess.user.model.UserSourcingEvent

suspend inline fun UserRepository.getAndStoreMap(
    userId: UserId,
    actor: Actor,
    correlationId: CorrelationId,
    log: Logger,
    functionName: String,
    domainFunction: (User) -> UserSourcingEvent
): Either<Error, Unit> {
    return this.getAndStoreFlatMap(userId, actor, correlationId, log, functionName) {
        domainFunction(it).right()
    }
}

suspend inline fun UserRepository.getAndStoreFlatMap(
    userId: UserId,
    actor: Actor,
    correlationId: CorrelationId,
    log: Logger,
    functionName: String,
    domainFunction: (User) -> Either<Error, UserSourcingEvent>
): Either<Error, Unit> {
    return log.logError(actor, correlationId, functionName) {
        getById(userId)
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