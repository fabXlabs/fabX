package cloud.fabX.fabXaccess.user.application

import arrow.core.Either
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.user.model.Admin
import cloud.fabX.fabXaccess.user.model.GettingUserByIdentity
import cloud.fabX.fabXaccess.user.model.PhoneNrIdentity
import cloud.fabX.fabXaccess.user.model.UserRepository
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Service to add a [PhoneNrIdentity] to a user.
 */
@OptIn(ExperimentalTime::class)
class AddingPhoneNrIdentity(
    loggerFactory: LoggerFactory,
    private val userRepository: UserRepository,
    private val gettingUserByIdentity: GettingUserByIdentity,
    private val clock: Clock
) {
    private val log: Logger = loggerFactory.invoke(this::class.java)

    suspend fun addPhoneNrIdentity(
        actor: Admin,
        correlationId: CorrelationId,
        userId: UserId,
        phoneNr: String
    ): Either<Error, Unit> =
        userRepository.getAndStoreFlatMap(userId, actor, correlationId, log, "addPhoneNrIdentity") {
            it.addPhoneNrIdentity(
                actor,
                clock,
                correlationId,
                phoneNr,
                gettingUserByIdentity
            )
        }
}