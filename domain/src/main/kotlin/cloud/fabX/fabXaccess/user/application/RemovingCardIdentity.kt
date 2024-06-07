package cloud.fabX.fabXaccess.user.application

import arrow.core.Either
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.user.model.Admin
import cloud.fabX.fabXaccess.user.model.CardIdentity
import cloud.fabX.fabXaccess.user.model.UserRepository
import kotlinx.datetime.Clock

/**
 * Service to remove a [CardIdentity] from a user.
 */
class RemovingCardIdentity(
    loggerFactory: LoggerFactory,
    private val userRepository: UserRepository,
    private val clock: Clock
) {
    private val log: Logger = loggerFactory.invoke(this::class.java)

    suspend fun removeCardIdentity(
        actor: Admin,
        correlationId: CorrelationId,
        userId: UserId,
        cardId: String
    ): Either<Error, Unit> =
        userRepository.getAndStoreFlatMap(userId, actor, correlationId, log, "removeCardIdentity") {
            it.removeCardIdentity(
                actor,
                clock,
                correlationId,
                cardId
            )
        }
}