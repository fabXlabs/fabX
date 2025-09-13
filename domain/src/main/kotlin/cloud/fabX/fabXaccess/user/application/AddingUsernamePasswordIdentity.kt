package cloud.fabX.fabXaccess.user.application

import arrow.core.Either
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.user.model.Admin
import cloud.fabX.fabXaccess.user.model.GettingUserByUsername
import cloud.fabX.fabXaccess.user.model.UserRepository
import cloud.fabX.fabXaccess.user.model.UsernamePasswordIdentity
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Service to add a [UsernamePasswordIdentity] to a user.
 */
@OptIn(ExperimentalTime::class)
class AddingUsernamePasswordIdentity(
    loggerFactory: LoggerFactory,
    private val userRepository: UserRepository,
    private val gettingUserByUsername: GettingUserByUsername,
    private val clock: Clock
) {
    private val log: Logger = loggerFactory.invoke(this::class.java)

    suspend fun addUsernamePasswordIdentity(
        actor: Admin,
        correlationId: CorrelationId,
        userId: UserId,
        username: String,
        hash: String
    ): Either<Error, Unit> =
        userRepository.getAndStoreFlatMap(userId, actor, correlationId, log, "addUsernamePasswordIdentity") {
            it.addUsernamePasswordIdentity(
                actor,
                clock,
                correlationId,
                username,
                hash,
                gettingUserByUsername
            )
        }
}