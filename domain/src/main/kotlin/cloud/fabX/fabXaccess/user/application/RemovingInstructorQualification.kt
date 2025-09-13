package cloud.fabX.fabXaccess.user.application

import arrow.core.Either
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.DomainEvent
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.model.QualificationId
import cloud.fabX.fabXaccess.common.model.SystemActor
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.qualification.model.Qualification
import cloud.fabX.fabXaccess.user.model.Admin
import cloud.fabX.fabXaccess.user.model.UserRepository
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Service to remove an instructor [Qualification] from a user.
 */
@OptIn(ExperimentalTime::class)
class RemovingInstructorQualification(
    loggerFactory: LoggerFactory,
    private val userRepository: UserRepository,
    private val clock: Clock
) {
    private val log: Logger = loggerFactory.invoke(this::class.java)

    suspend fun removeInstructorQualification(
        actor: Admin,
        correlationId: CorrelationId,
        userId: UserId,
        qualificationId: QualificationId
    ): Either<Error, Unit> =
        userRepository.getAndStoreFlatMap(userId, actor, correlationId, log, "removeInstructorQualification") {
            it.removeInstructorQualification(
                actor,
                clock,
                correlationId,
                qualificationId
            )
        }

    internal suspend fun removeInstructorQualification(
        domainEvent: DomainEvent,
        userId: UserId,
        qualificationId: QualificationId
    ): Either<Error, Unit> =
        userRepository.getAndStoreFlatMap(
            userId,
            SystemActor,
            domainEvent.correlationId,
            log,
            "removeInstructorQualification"
        ) {
            it.removeInstructorQualification(
                domainEvent,
                clock,
                qualificationId
            )
        }
}