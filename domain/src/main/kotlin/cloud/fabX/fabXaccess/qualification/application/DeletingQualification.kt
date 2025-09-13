package cloud.fabX.fabXaccess.qualification.application

import arrow.core.Either
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.DomainEventPublisher
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.QualificationDeleted
import cloud.fabX.fabXaccess.common.model.QualificationId
import cloud.fabX.fabXaccess.qualification.model.QualificationRepository
import cloud.fabX.fabXaccess.tool.model.GettingToolsByQualificationId
import cloud.fabX.fabXaccess.user.model.Admin
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Service to handle deleting a qualification.
 */
@OptIn(ExperimentalTime::class)
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
    ): Either<Error, Unit> =
        qualificationRepository.getAndStoreFlatMap(
            qualificationId,
            actor,
            correlationId,
            log,
            "deleteQualification"
        ) {
            it.delete(actor, clock, correlationId, gettingToolsByQualificationId)
        }
            .onRight {
                domainEventPublisher.publish(
                    QualificationDeleted(
                        actor.id,
                        clock.now(),
                        correlationId,
                        qualificationId
                    )
                )
            }
}