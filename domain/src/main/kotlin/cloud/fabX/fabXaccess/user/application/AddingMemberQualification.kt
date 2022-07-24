package cloud.fabX.fabXaccess.user.application

import arrow.core.Option
import arrow.core.flatMap
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.model.QualificationId
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.qualification.model.Qualification
import cloud.fabX.fabXaccess.qualification.model.QualificationRepository
import cloud.fabX.fabXaccess.user.model.Instructor
import cloud.fabX.fabXaccess.user.model.UserRepository

/**
 * Service to add a member [Qualification] to a user.
 */
class AddingMemberQualification(
    loggerFactory: LoggerFactory,
    private val qualificationRepository: QualificationRepository,
    private val userRepository: UserRepository
) {
    private val log: Logger = loggerFactory.invoke(this::class.java)

    fun addMemberQualification(
        actor: Instructor,
        correlationId: CorrelationId,
        userId: UserId,
        qualificationId: QualificationId
    ): Option<Error> {
        log.debug("addMemberQualification...")

        return userRepository.getById(userId)
            .flatMap {
                it.addMemberQualification(
                    actor,
                    correlationId,
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