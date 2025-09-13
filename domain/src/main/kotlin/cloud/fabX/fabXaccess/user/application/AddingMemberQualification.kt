package cloud.fabX.fabXaccess.user.application

import arrow.core.Either
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.model.QualificationId
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.qualification.model.GettingQualificationById
import cloud.fabX.fabXaccess.qualification.model.Qualification
import cloud.fabX.fabXaccess.user.model.Instructor
import cloud.fabX.fabXaccess.user.model.UserRepository
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Service to add a member [Qualification] to a user.
 */
@OptIn(ExperimentalTime::class)
class AddingMemberQualification(
    loggerFactory: LoggerFactory,
    private val userRepository: UserRepository,
    private val gettingQualificationById: GettingQualificationById,
    private val clock: Clock
) {
    private val log: Logger = loggerFactory.invoke(this::class.java)

    suspend fun addMemberQualification(
        actor: Instructor,
        correlationId: CorrelationId,
        userId: UserId,
        qualificationId: QualificationId
    ): Either<Error, Unit> =
        userRepository.getAndStoreFlatMap(userId, actor, correlationId, log, "addMemberQualification") {
            it.addMemberQualification(
                actor,
                clock,
                correlationId,
                qualificationId,
                gettingQualificationById
            )
        }
}