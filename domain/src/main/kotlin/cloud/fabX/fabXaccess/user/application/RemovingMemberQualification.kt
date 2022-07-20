package cloud.fabX.fabXaccess.user.application

import arrow.core.Either
import arrow.core.Option
import arrow.core.flatMap
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.application.logger
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.DomainEvent
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.QualificationId
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.qualification.model.Qualification
import cloud.fabX.fabXaccess.user.model.Admin
import cloud.fabX.fabXaccess.user.model.User
import cloud.fabX.fabXaccess.user.model.UserSourcingEvent

/**
 * Service to remove a member [Qualification] from a user.
 */
class RemovingMemberQualification {

    private val log = logger()
    private val userRepository = DomainModule.userRepository()

    fun removeMemberQualification(
        actor: Admin,
        correlationId: CorrelationId,
        userId: UserId,
        qualificationId: QualificationId
    ): Option<Error> =
        removeMemberQualification(userId) { it.removeMemberQualification(actor, correlationId, qualificationId) }

    internal fun removeMemberQualification(
        domainEvent: DomainEvent,
        userId: UserId,
        qualificationId: QualificationId
    ): Option<Error> =
        removeMemberQualification(userId) { it.removeMemberQualification(domainEvent, qualificationId) }

    private fun removeMemberQualification(
        userId: UserId,
        domainMethod: (User) -> Either<Error, UserSourcingEvent>
    ): Option<Error> {
        log.debug("removeMemberQualification...")

        return userRepository.getById(userId)
            .flatMap { domainMethod(it) }
            .flatMap {
                userRepository.store(it)
                    .toEither { }
                    .swap()
            }
            .swap()
            .orNone()
            .tapNone { log.debug("...removeMemberQualification done") }
            .tap { log.error("...removeMemberQualification error: $it") }
    }
}