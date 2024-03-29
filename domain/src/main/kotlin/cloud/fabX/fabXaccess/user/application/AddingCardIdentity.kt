package cloud.fabX.fabXaccess.user.application

import arrow.core.Option
import arrow.core.flatMap
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.user.model.Admin
import cloud.fabX.fabXaccess.user.model.CardIdentity
import cloud.fabX.fabXaccess.user.model.GettingUserByCardId
import cloud.fabX.fabXaccess.user.model.UserRepository
import kotlinx.datetime.Clock

/**
 * Service to add a [CardIdentity] to a user.
 */
class AddingCardIdentity(
    loggerFactory: LoggerFactory,
    private val userRepository: UserRepository,
    private val gettingUserByCardId: GettingUserByCardId,
    private val clock: Clock
) {
    private val log: Logger = loggerFactory.invoke(this::class.java)

    suspend fun addCardIdentity(
        actor: Admin,
        correlationId: CorrelationId,
        userId: UserId,
        cardId: String,
        cardSecret: String
    ): Option<Error> {
        log.debug("addCardIdentity...")

        return userRepository.getById(userId)
            .flatMap {
                it.addCardIdentity(
                    actor,
                    clock,
                    correlationId,
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
            .getOrNone()
            .onNone { log.debug("...addCardIdentity done") }
            .onSome { log.error("...addCardIdentity error: $it") }
    }
}