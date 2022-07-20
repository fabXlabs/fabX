package cloud.fabX.fabXaccess.qualification.application

import arrow.core.Either
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.application.logger
import cloud.fabX.fabXaccess.common.model.Actor
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.QualificationId
import cloud.fabX.fabXaccess.qualification.model.Qualification

/**
 * Service to get qualifications.
 */
class GettingQualification {

    private val log = logger()
    private val qualificationRepository = DomainModule.qualificationRepository()

    fun getAll(
        actor: Actor,
        correlationId: CorrelationId
    ): Set<Qualification> {
        log.debug("getAll (actor: $actor, correlationId: $correlationId)...")

        return qualificationRepository.getAll()
    }

    fun getById(
        actor: Actor,
        correlationId: CorrelationId,
        qualificationId: QualificationId
    ): Either<Error, Qualification> {
        log.debug("getById  (actor: $actor, correlationId: $correlationId)...")

        return qualificationRepository.getById(qualificationId)
    }
}