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
 * Service to remove a [CardIdentity] from a user.
 */
class RemovingCardIdentity {

    private val log = logger()
    private val userRepository = DomainModule.userRepository()

    fun removeCardIdentity(
        actor: Admin,
        userId: UserId,
        cardId: String
    ): Option<Error> {
        log.debug("removeCardIdentity...")

        return userRepository.getById(userId)
            .flatMap {
                it.removeCardIdentity(
                    actor,
                    cardId
                )
            }
            .flatMap {
                userRepository.store(it)
                    .toEither { }
                    .swap()
            }
            .swap()
            .orNone()
            .tapNone { log.debug("...removeCardIdentity done") }
            .tap { log.error("...removeCardIdentity error: $it") }
    }
}