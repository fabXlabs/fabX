package cloud.fabX.fabXaccess.user.application

import arrow.core.Either
import arrow.core.Option
import arrow.core.flatMap
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.DomainEvent
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.model.QualificationId
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.qualification.model.Qualification
import cloud.fabX.fabXaccess.user.model.Admin
import cloud.fabX.fabXaccess.user.model.User
import cloud.fabX.fabXaccess.user.model.UserRepository
import cloud.fabX.fabXaccess.user.model.UserSourcingEvent
import kotlinx.datetime.Clock

/**
 * Service to remove an instructor [Qualification] from a user.
 */
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
    ): Option<Error> {
        return removeInstructorQualification(userId) {
            it.removeInstructorQualification(
                actor,
                clock,
                correlationId,
                qualificationId
            )
        }
    }

    internal suspend fun removeInstructorQualification(
        domainEvent: DomainEvent,
        userId: UserId,
        qualificationId: QualificationId
    ): Option<Error> {
        return removeInstructorQualification(userId) {
            it.removeInstructorQualification(
                domainEvent,
                clock,
                qualificationId
            )
        }
    }

    private suspend fun removeInstructorQualification(
        userId: UserId,
        domainMethod: (User) -> Either<Error, UserSourcingEvent>
    ): Option<Error> {
        log.debug("removeInstructorQualification...")

        return userRepository.getById(userId)
            .flatMap { domainMethod(it) }
            .flatMap {
                userRepository.store(it)
                    .toEither { }
                    .swap()
            }
            .swap()
            .getOrNone()
            .onNone { log.debug("...removeInstructorQualification done") }
            .onSome { log.error("...removeInstructorQualification error: $it") }
    }
}