package cloud.fabX.fabXaccess.user.application

import arrow.core.Either
import arrow.core.flatMap
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.user.model.Member
import cloud.fabX.fabXaccess.user.model.UserRepository
import kotlinx.datetime.Clock

/**
 * Service for changing password of a user.
 */
class ChangingPassword(
    loggerFactory: LoggerFactory,
    private val userRepository: UserRepository,
    private val clock: Clock
) {
    private val log: Logger = loggerFactory.invoke(this::class.java)

    suspend fun changeOwnPassword(
        actor: Member,
        correlationId: CorrelationId,
        userId: UserId,
        hash: String
    ): Either<Error, Unit> {
        log.debug("changeOwnPassword...")

        return userRepository.getById(userId)
            .flatMap {
                it.changePassword(
                    actor,
                    clock,
                    correlationId,
                    hash
                )
            }
            .flatMap {
                userRepository.store(it)
                    .toEither { }
                    .swap()
            }
            .tap { log.debug("...changeOwnPassword done") }
            .tapLeft { log.error("...changeOwnPassword error: $it") }
    }
}