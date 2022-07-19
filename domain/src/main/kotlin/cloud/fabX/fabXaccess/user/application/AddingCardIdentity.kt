package cloud.fabX.fabXaccess.user.application

import arrow.core.Option
import arrow.core.flatMap
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.application.logger
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.user.model.Admin
import cloud.fabX.fabXaccess.user.model.CardIdentity
import cloud.fabX.fabXaccess.user.model.UserId

/**
 * Service to add a [CardIdentity] to a user.
 */
class AddingCardIdentity {

    private val log = logger()
    private val userRepository = DomainModule.userRepository()
    private val gettingUserByCardId = DomainModule.gettingUserByCardId()

    fun addCardIdentity(
        actor: Admin,
        userId: UserId,
        cardId: String,
        cardSecret: String
    ): Option<Error> {
        log.debug("addCardIdentity...")

        return userRepository.getById(userId)
            .flatMap {
                it.addCardIdentity(
                    actor,
                    cardId,
                    cardSecret,
                    gettingUserByCardId
                )
            }
            .flatMap {
                userRepository.store(it)
                    .toEither { }
                    .swap()
            }
            .swap()
            .orNone()
            .tapNone { log.debug("...addCardIdentity done") }
            .tap { log.error("...addCardIdentity error: $it") }
    }
}