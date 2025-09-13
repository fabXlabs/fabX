package cloud.fabX.fabXaccess.user.application

import arrow.core.Either
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.user.model.Admin
import cloud.fabX.fabXaccess.user.model.UserRepository
import cloud.fabX.fabXaccess.user.model.UsernamePasswordIdentity
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Service to remove a [UsernamePasswordIdentity] from a user.
 */
@OptIn(ExperimentalTime::class)
class RemovingUsernamePasswordIdentity(
    loggerFactory: LoggerFactory,
    private val userRepository: UserRepository,
    private val clock: Clock
) {
    private val log: Logger = loggerFactory.invoke(this::class.java)

    suspend fun removeUsernamePasswordIdentity(
        actor: Admin,
        correlationId: CorrelationId,
        userId: UserId,
        username: String
    ): Either<Error, Unit> =
        userRepository.getAndStoreFlatMap(userId, actor, correlationId, log, "removeUsernamePasswordIdentity") {
            it.removeUsernamePasswordIdentity(
                actor,
                clock,
                correlationId,
                username
            )
        }
}