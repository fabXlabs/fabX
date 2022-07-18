package cloud.fabX.fabXaccess.user.application

import arrow.core.Option
import arrow.core.flatMap
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.application.logger
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.user.model.Admin
import cloud.fabX.fabXaccess.user.model.UserId

/**
 * Service to handle changing whether a user is admin.
 */
class ChangingIsAdmin {

    private val log = logger()
    private val userRepository = DomainModule.userRepository()

    fun changeIsAdmin(
        actor: Admin,
        userId: UserId,
        isAdmin: Boolean
    ): Option<Error> {
        log.debug("changeIsAdmin...")

        return userRepository.getById(userId)
            .flatMap {
                it.changeIsAdmin(
                    actor,
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