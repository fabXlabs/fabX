package cloud.fabX.fabXaccess.user.application

import arrow.core.Option
import arrow.core.flatMap
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.application.logger
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.user.model.Admin
import cloud.fabX.fabXaccess.user.model.UserId
import cloud.fabX.fabXaccess.user.model.UsernamePasswordIdentity

/**
 * Service to add a [UsernamePasswordIdentity] to a user.
 */
class AddingUsernamePasswordIdentity {

    private val log = logger()
    private val userRepository = DomainModule.userRepository()

    fun addUsernamePasswordIdentity(
        actor: Admin,
        userId: UserId,
        username: String,
        password: String
    ): Option<Error> {
        log.debug("addUsernamePasswordIdentity...")

        return userRepository.getById(userId)
            .map {
                it.addUsernamePasswordIdentity(
                    actor,
                    username,
                    password
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