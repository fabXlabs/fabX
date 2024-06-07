package cloud.fabX.fabXaccess.qualification.application

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.right
import cloud.fabX.fabXaccess.common.model.Actor
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.model.QualificationId
import cloud.fabX.fabXaccess.qualification.model.Qualification
import cloud.fabX.fabXaccess.qualification.model.QualificationRepository
import cloud.fabX.fabXaccess.qualification.model.QualificationSourcingEvent

suspend inline fun QualificationRepository.getAndStoreMap(
    qualificationId: QualificationId,
    actor: Actor,
    correlationId: CorrelationId,
    log: Logger,
    functionName: String,
    domainFunction: (Qualification) -> QualificationSourcingEvent
): Either<Error, Unit> {
    return this.getAndStoreFlatMap(qualificationId, actor, correlationId, log, functionName) {
        domainFunction(it).right()
    }
}

suspend inline fun QualificationRepository.getAndStoreFlatMap(
    qualificationId: QualificationId,
    actor: Actor,
    correlationId: CorrelationId,
    log: Logger,
    functionName: String,
    domainFunction: (Qualification) -> Either<Error, QualificationSourcingEvent>
): Either<Error, Unit> {
    return log.logError(actor, correlationId, functionName) {
        getById(qualificationId)
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