package cloud.fabX.fabXaccess.user.application

import arrow.core.Either
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.Actor
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.device.model.DeviceActor
import cloud.fabX.fabXaccess.user.model.Admin
import cloud.fabX.fabXaccess.user.model.CardIdentity
import cloud.fabX.fabXaccess.user.model.GettingUserByCardId
import cloud.fabX.fabXaccess.user.model.UserRepository
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Service to add a [CardIdentity] to a user.
 */
@OptIn(ExperimentalTime::class)
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
    ): Either<Error, Unit> =
        userRepository.getAndStoreFlatMap(userId, actor, correlationId, log, "addCardIdentity") {
            it.addCardIdentity(
                actor,
                clock,
                correlationId,
                cardId,
                cardSecret,
                gettingUserByCardId
            )
        }
}