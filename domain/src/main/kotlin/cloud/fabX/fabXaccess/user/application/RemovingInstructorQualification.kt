package cloud.fabX.fabXaccess.user.application

import arrow.core.Either
import arrow.core.Option
import arrow.core.flatMap
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.application.logger
import cloud.fabX.fabXaccess.common.model.DomainEvent
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.qualification.model.Qualification
import cloud.fabX.fabXaccess.qualification.model.QualificationId
import cloud.fabX.fabXaccess.user.model.Admin
import cloud.fabX.fabXaccess.user.model.User
import cloud.fabX.fabXaccess.user.model.UserId
import cloud.fabX.fabXaccess.user.model.UserSourcingEvent

/**
 * Service to remove an instructor [Qualification] from a user.
 */
class RemovingInstructorQualification {

    private val log = logger()
    private val userRepository = DomainModule.userRepository()

    fun removeInstructorQualification(
        actor: Admin,
        userId: UserId,
        qualificationId: QualificationId
    ): Option<Error> {
        return removeInstructorQualification(userId) {
            it.removeInstructorQualification(
                actor,
                qualificationId
            )
        }
    }

    internal fun removeInstructorQualification(
        domainEvent: DomainEvent,
        userId: UserId,
        qualificationId: QualificationId
    ): Option<Error> {
        return removeInstructorQualification(userId) {
            it.removeInstructorQualification(
                domainEvent,
                qualificationId
            )
        }
    }

    private fun removeInstructorQualification(
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
            .orNone()
            .tapNone { log.debug("...removeInstructorQualification done") }
            .tap { log.error("...removeInstructorQualification error: $it") }
    }
}