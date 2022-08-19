package cloud.fabX.fabXaccess.user.application

import arrow.core.Option
import arrow.core.flatMap
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.user.model.Admin
import cloud.fabX.fabXaccess.user.model.UserRepository
import cloud.fabX.fabXaccess.user.model.WebauthnIdentity
import kotlinx.datetime.Clock

/**
 * Service to remove a [WebauthnIdentity] to a user.
 */
class RemovingWebauthnIdentity(
    loggerFactory: LoggerFactory,
    private val userRepository: UserRepository,
    private val clock: Clock
) {
    private val log: Logger = loggerFactory.invoke(this::class.java)

    suspend fun removeWebauthnIdentity(
        actor: Admin,
        correlationId: CorrelationId,
        userId: UserId,
        credentialId: ByteArray
    ): Option<Error> {
        log.debug("removeWebauthnIdentity...")

        return userRepository.getById(userId)
            .flatMap {
                it.removeWebauthnIdentity(
                    actor,
                    clock,
                    correlationId,
                    credentialId
                )
            }
            .flatMap {
                userRepository.store(it)
                    .toEither { }
                    .swap()
            }
            .swap()
            .orNone()
            .tapNone { log.debug("...removeWebauthnIdentity done") }
            .tap { log.error("...removeWebauthnIdentity error: $it") }
    }
}