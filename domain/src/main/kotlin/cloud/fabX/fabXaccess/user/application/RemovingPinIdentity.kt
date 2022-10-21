package cloud.fabX.fabXaccess.user.application

import arrow.core.Option
import arrow.core.flatMap
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.user.model.Admin
import cloud.fabX.fabXaccess.user.model.PinIdentity
import cloud.fabX.fabXaccess.user.model.UserRepository
import kotlinx.datetime.Clock

/**
 * Service to remove a [PinIdentity] from a user.
 */
class RemovingPinIdentity(
    loggerFactory: LoggerFactory,
    private val userRepository: UserRepository,
    private val clock: Clock
) {
    private val log: Logger = loggerFactory.invoke(this::class.java)

    suspend fun removePinIdentity(
        actor: Admin,
        correlationId: CorrelationId,
        userId: UserId
    ): Option<Error> {
        log.debug("removePinIdentity...")

        return userRepository.getById(userId)
            .flatMap {
                it.removePinIdentity(
                    actor,
                    clock,
                    correlationId
                )
            }
            .flatMap {
                userRepository.store(it)
                    .toEither { }
                    .swap()
            }
            .swap()
            .orNone()
            .tapNone { log.debug("...removePinIdentity done") }
            .tap { log.error("...removePinIdentity error: $it") }
    }
}