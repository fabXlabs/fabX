package cloud.fabX.fabXaccess.user.application

import arrow.core.Either
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.model.SystemActor
import cloud.fabX.fabXaccess.user.model.GettingUserByIdentity
import cloud.fabX.fabXaccess.user.model.User
import cloud.fabX.fabXaccess.user.model.UserIdentity

/**
 * Service to get users by identity.
 */
class GettingUserByIdentity(
    loggerFactory: LoggerFactory,
    private val gettingUserByIdentity: GettingUserByIdentity
) {
    private val log: Logger = loggerFactory.invoke(this::class.java)

    suspend fun getUserByIdentity(
        actor: SystemActor,
        correlationId: CorrelationId,
        identity: UserIdentity
    ): Either<Error, User> {
        log.debug("getUserByIdentity (actor: $actor, correlationId: $correlationId)...")

        return gettingUserByIdentity.getByIdentity(identity)
    }

    suspend fun getUserByIdentity(
        actor: SystemActor,
        identity: UserIdentity
    ): Either<Error, User> {
        log.debug("getUserByIdentity (actor: $actor)...")

        return gettingUserByIdentity.getByIdentity(identity)
    }
}