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
 * Service to add an instructor [Qualification] to a user.
 */
class AddingInstructorQualification {

    private val log = logger()
    private val userRepository = DomainModule.userRepository()
    private val qualificationRepository = DomainModule.qualificationRepository()

    fun addInstructorQualification(
        actor: Admin,
        userId: UserId,
        qualificationId: QualificationId
    ): Option<Error> {
        log.debug("addInstructorQualification...")

        return userRepository.getById(userId)
            .flatMap {
                it.addInstructorQualification(
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
            .tapNone { log.debug("...addInstructorQualification done") }
            .tap { log.error("...addInstructorQualification error: $it") }
    }
}