package cloud.fabX.fabXaccess.user.application

import arrow.core.Either
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.user.model.Admin
import cloud.fabX.fabXaccess.user.model.UserRepository
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Service to handle changing whether a user is admin.
 */
@OptIn(ExperimentalTime::class)
class ChangingIsAdmin(
    loggerFactory: LoggerFactory,
    private val userRepository: UserRepository,
    private val clock: Clock
) {
    private val log: Logger = loggerFactory.invoke(this::class.java)

    suspend fun changeIsAdmin(
        actor: Admin,
        correlationId: CorrelationId,
        userId: UserId,
        isAdmin: Boolean
    ): Either<Error, Unit> =
        userRepository.getAndStoreFlatMap(userId, actor, correlationId, log, "changeIsAdmin") {
            it.changeIsAdmin(
                actor,
                clock,
                correlationId,
                isAdmin
            )
        }
}