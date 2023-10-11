package cloud.fabX.fabXaccess.user.application

import arrow.core.Option
import arrow.core.flatMap
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.model.QualificationId
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.qualification.model.GettingQualificationById
import cloud.fabX.fabXaccess.qualification.model.Qualification
import cloud.fabX.fabXaccess.user.model.Admin
import cloud.fabX.fabXaccess.user.model.UserRepository
import kotlinx.datetime.Clock

/**
 * Service to add an instructor [Qualification] to a user.
 */
class AddingInstructorQualification(
    loggerFactory: LoggerFactory,
    private val userRepository: UserRepository,
    private val gettingQualificationById: GettingQualificationById,
    private val clock: Clock
) {
    private val log: Logger = loggerFactory.invoke(this::class.java)

    suspend fun addInstructorQualification(
        actor: Admin,
        correlationId: CorrelationId,
        userId: UserId,
        qualificationId: QualificationId
    ): Option<Error> {
        log.debug("addInstructorQualification...")

        return userRepository.getById(userId)
            .flatMap {
                it.addInstructorQualification(
                    actor,
                    clock,
                    correlationId,
                    qualificationId,
                    gettingQualificationById
                )
            }
            .flatMap {
                userRepository.store(it)
                    .toEither { }
                    .swap()
            }
            .swap()
            .getOrNone()
            .onNone { log.debug("...addInstructorQualification done") }
            .onSome { log.error("...addInstructorQualification error: $it") }
    }
}