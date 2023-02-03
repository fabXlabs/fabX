package cloud.fabX.fabXaccess.user.application

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import arrow.core.toOption
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.device.model.DeviceActor
import cloud.fabX.fabXaccess.user.model.UserIdentity
import cloud.fabX.fabXaccess.user.model.UserRepository

/**
 * Service to validate a user's second factor.
 */
class ValidatingSecondFactor(
    loggerFactory: LoggerFactory,
    private val userRepository: UserRepository
) {
    private val log: Logger = loggerFactory.invoke(this::class.java)

    suspend fun validateSecondFactor(
        actor: DeviceActor,
        correlationId: CorrelationId,
        secondIdentity: UserIdentity
    ): Either<Error, Unit> {
        log.debug("validateSecondFactor (actor: $actor, correlationId: $correlationId)...")

        return actor.onBehalfOf.toOption()
            .toEither { Error.NotAuthenticated("Required authentication not found.", correlationId) }
            .map { it.userId }
            .flatMap { onBehalfOfId ->
                userRepository.getById(onBehalfOfId)
                    .flatMap {
                        if (!it.identities.contains(secondIdentity)) {
                            Error.InvalidSecondFactor("Invalid second factor provided.", correlationId).left()
                        } else {
                            Unit.right()
                        }
                    }
            }
    }
}