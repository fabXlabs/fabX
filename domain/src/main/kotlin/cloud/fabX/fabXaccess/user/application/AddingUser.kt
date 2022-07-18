package cloud.fabX.fabXaccess.user.application

import arrow.core.Option
import arrow.core.flatMap
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.application.logger
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.user.model.Admin
import cloud.fabX.fabXaccess.user.model.User

/**
 * Service to add new users.
 */
class AddingUser {

    private val log = logger()
    private val userRepository = DomainModule.userRepository()
    private val gettingUserByWikiName = DomainModule.gettingUserByWikiName()

    fun addUser(
        actor: Admin,
        firstName: String,
        lastName: String,
        wikiName: String
    ): Option<Error> {
        log.debug("addUser...")

        return User
            .addNew(
                actor,
                firstName,
                lastName,
                wikiName,
                gettingUserByWikiName
            )
            .flatMap {
                userRepository.store(it)
                    .toEither { }
                    .swap()
            }
            .swap()
            .orNone()
            .tapNone { log.debug("...addUser done") }
            .tap { log.error("...addUser error: $it") }
    }
}