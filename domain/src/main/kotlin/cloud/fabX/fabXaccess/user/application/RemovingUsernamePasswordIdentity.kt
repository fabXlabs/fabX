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

/**
 * Service to remove a [UsernamePasswordIdentity] from a user.
 */
class RemovingUsernamePasswordIdentity(
    loggerFactory: LoggerFactory,
    private val userRepository: UserRepository
) {
    private val log: Logger = loggerFactory.invoke(this::class.java)

    fun removeUsernamePasswordIdentity(
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
            .orNone()
            .tapNone { log.debug("...removeUsernamePasswordIdentity done") }
            .tap { log.error("...removeUsernamePasswordIdentity error: $it") }
    }
}