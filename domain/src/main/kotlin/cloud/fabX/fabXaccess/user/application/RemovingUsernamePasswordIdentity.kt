package cloud.fabX.fabXaccess.user.application

import arrow.core.Option
import arrow.core.flatMap
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.user.model.Admin
import cloud.fabX.fabXaccess.user.model.UserRepository
import cloud.fabX.fabXaccess.user.model.UsernamePasswordIdentity
import kotlinx.datetime.Clock

/**
 * Service to remove a [UsernamePasswordIdentity] from a user.
 */
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
    ): Option<Error> {
        log.debug("removeUsernamePasswordIdentity...")

        return userRepository.getById(userId)
            .flatMap {
                it.removeUsernamePasswordIdentity(
                    actor,
                    clock,
                    correlationId,
                    username
                )
            }
            .flatMap {
                userRepository.store(it)
                    .toEither { }
                    .swap()
            }
            .swap()
            .getOrNone()
            .onNone { log.debug("...removeUsernamePasswordIdentity done") }
            .onSome { log.error("...removeUsernamePasswordIdentity error: $it") }
    }
}