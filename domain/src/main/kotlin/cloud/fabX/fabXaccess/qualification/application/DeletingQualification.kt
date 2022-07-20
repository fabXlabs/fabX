package cloud.fabX.fabXaccess.qualification.application

import arrow.core.Option
import arrow.core.flatMap
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.application.logger
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.QualificationDeleted
import cloud.fabX.fabXaccess.common.model.QualificationId
import cloud.fabX.fabXaccess.user.model.Admin

/**
 * Service to handle deleting a qualification.
 */
class DeletingQualification {

    private val log = logger()
    private val clock = DomainModule.clock()
    private val domainEventPublisher = DomainModule.domainEventPublisher()
    private val qualificationRepository = DomainModule.qualificationRepository()
    private val gettingToolsByQualificationId = DomainModule.gettingToolsQualificationId()

    fun deleteQualification(
        actor: Admin,
        correlationId: CorrelationId,
        qualificationId: QualificationId
    ): Option<Error> {
        log.debug("deleteQualification...")

        return qualificationRepository.getById(qualificationId)
            .flatMap {
                it.delete(actor, correlationId, gettingToolsByQualificationId)
            }
            .flatMap {
                qualificationRepository.store(it)
                    .toEither { }
                    .swap()
            }
            .swap()
            .orNone()
            .tapNone { log.debug("...deleteQualification done") }
            .tapNone {
                domainEventPublisher.publish(
                    QualificationDeleted(
                        actor.id,
                        clock.now(),
                        correlationId,
                        qualificationId
                    )
                )
            }
            .tap { log.error("...deleteQualification error: $it") }
    }
}