package cloud.fabX.fabXaccess.user.application

import arrow.core.Option
import arrow.core.flatMap
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.application.logger
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.qualification.model.Qualification
import cloud.fabX.fabXaccess.qualification.model.QualificationId
import cloud.fabX.fabXaccess.user.model.Instructor
import cloud.fabX.fabXaccess.user.model.UserId

/**
 * Service to add a member [Qualification] to a user.
 */
class AddingMemberQualification {

    private val log = logger()
    private val userRepository = DomainModule.userRepository()
    private val qualificationRepository = DomainModule.qualificationRepository()

    fun addMemberQualification(
        actor: Instructor,
        userId: UserId,
        qualificationId: QualificationId
    ): Option<Error> {
        log.debug("addMemberQualification...")

        return userRepository.getById(userId)
            .flatMap {
                it.addMemberQualification(
                    actor,
                    qualificationId,
                    qualificationRepository
                )
            }
            .flatMap {
                userRepository.store(it)
                    .toEither { }
                    .swap()
            }
            .swap()
            .orNone()
            .tapNone { log.debug("...addMemberQualification done") }
            .tap { log.error("...addMemberQualification error: $it") }
    }
}