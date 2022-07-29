package cloud.fabX.fabXaccess.user.application

import arrow.core.Option
import arrow.core.flatMap
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.user.model.Admin
import cloud.fabX.fabXaccess.user.model.GettingUserByUsername
import cloud.fabX.fabXaccess.user.model.UserRepository
import cloud.fabX.fabXaccess.user.model.UsernamePasswordIdentity
import kotlinx.datetime.Clock

/**
 * Service to add a [UsernamePasswordIdentity] to a user.
 */
class AddingUsernamePasswordIdentity(
    loggerFactory: LoggerFactory,
    private val userRepository: UserRepository,
    private val gettingUserByUsername: GettingUserByUsername,
    private val clock: Clock
) {
    private val log: Logger = loggerFactory.invoke(this::class.java)

    fun addUsernamePasswordIdentity(
        actor: Admin,
        correlationId: CorrelationId,
        userId: UserId,
        username: String,
        hash: String
    ): Option<Error> {
        log.debug("addUsernamePasswordIdentity...")

        return userRepository.getById(userId)
            .flatMap {
                it.addUsernamePasswordIdentity(
                    actor,
                    clock,
                    correlationId,
                    username,
                    hash,
                    gettingUserByUsername
                )
            }
            .flatMap {
                userRepository.store(it)
                    .toEither { }
                    .swap()
            }
            .swap()
            .orNone()
            .tapNone { log.debug("...addUsernamePasswordIdentity done") }
            .tap { log.error("...addUsernamePasswordIdentity error: $it") }
    }
}