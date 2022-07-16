package cloud.fabX.fabXaccess.user.application

import arrow.core.Option
import arrow.core.flatMap
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.application.logger
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.qualification.model.Qualification
import cloud.fabX.fabXaccess.qualification.model.QualificationId
import cloud.fabX.fabXaccess.user.model.Admin
import cloud.fabX.fabXaccess.user.model.UserId

/**
 * Service to remove a member [Qualification] from a user.
 */
class RemovingMemberQualification {

    private val log = logger()
    private val userRepository = DomainModule.userRepository()

    fun removeMemberQualification(
        actor: Admin,
        userId: UserId,
        qualificationId: QualificationId
    ): Option<Error> {
        log.debug("removeMemberQualification...")

        return userRepository.getById(userId)
            .flatMap {
                it.removeMemberQualification(
                    actor,
                    qualificationId
                )
            }
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