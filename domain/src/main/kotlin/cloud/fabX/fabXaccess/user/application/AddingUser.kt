package cloud.fabX.fabXaccess.user.application

import arrow.core.Either
import arrow.core.flatMap
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.application.logger
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.UserId
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
        correlationId: CorrelationId,
        firstName: String,
        lastName: String,
        wikiName: String
    ): Either<Error, UserId> {
        log.debug("addUser...")

        return User
            .addNew(
                actor,
                correlationId,
                firstName,
                lastName,
                wikiName,
                gettingUserByWikiName
            )
            .flatMap {
                userRepository.store(it)
                    .toEither { it.aggregateRootId }
                    .swap()
            }
            .tap { log.debug("...addUser done") }
            .tapLeft { log.error("...addUser error: $it") }
    }
}