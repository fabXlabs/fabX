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

/**
 * Service to handle changing whether a user is admin.
 */
class ChangingIsAdmin(
    loggerFactory: LoggerFactory,
    private val userRepository: UserRepository
) {
    private val log: Logger = loggerFactory.invoke(this::class.java)

    fun changeIsAdmin(
        actor: Admin,
        correlationId: CorrelationId,
        userId: UserId,
        isAdmin: Boolean
    ): Option<Error> {
        log.debug("changeIsAdmin...")

        return userRepository.getById(userId)
            .flatMap {
                it.changeIsAdmin(
                    actor,
                    correlationId,
                    isAdmin
                )
            }
            .flatMap {
                userRepository.store(it)
                    .toEither { }
                    .swap()
            }
            .swap()
            .orNone()
            .tapNone { log.debug("...changeIsAdmin done") }
            .tap { log.error("...changeIsAdmin error: $it") }
    }
}