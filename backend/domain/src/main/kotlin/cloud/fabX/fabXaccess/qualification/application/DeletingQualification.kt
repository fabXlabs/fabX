package cloud.fabX.fabXaccess.qualification.application

import arrow.core.Option
import arrow.core.flatMap
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.DomainEventPublisher
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.QualificationDeleted
import cloud.fabX.fabXaccess.common.model.QualificationId
import cloud.fabX.fabXaccess.qualification.model.QualificationRepository
import cloud.fabX.fabXaccess.tool.model.GettingToolsByQualificationId
import cloud.fabX.fabXaccess.user.model.Admin
import kotlinx.datetime.Clock

/**
 * Service to handle deleting a qualification.
 */
class DeletingQualification(
    loggerFactory: LoggerFactory,
    private val clock: Clock,
    private val domainEventPublisher: DomainEventPublisher,
    private val qualificationRepository: QualificationRepository,
    private val gettingToolsByQualificationId: GettingToolsByQualificationId
) {
    private val log = loggerFactory.invoke(this::class.java)

    suspend fun deleteQualification(
        actor: Admin,
        correlationId: CorrelationId,
        qualificationId: QualificationId
    ): Option<Error> {
        log.debug("deleteQualification...")

        return qualificationRepository.getById(qualificationId)
            .flatMap {
                it.delete(actor, clock, correlationId, gettingToolsByQualificationId)
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