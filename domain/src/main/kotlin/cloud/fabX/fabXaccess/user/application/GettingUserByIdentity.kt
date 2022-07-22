package cloud.fabX.fabXaccess.user.application

import arrow.core.Either
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.application.logger
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.SystemActor
import cloud.fabX.fabXaccess.user.model.User
import cloud.fabX.fabXaccess.user.model.UserIdentity

/**
 * Service to get users by identity.
 */
class GettingUserByIdentity {

    private val log = logger()
    private val gettingUserByIdentity = DomainModule.gettingUserByIdentity()

    fun getUserByIdentity(
        actor: SystemActor,
        correlationId: CorrelationId,
        identity: UserIdentity
    ): Either<Error, User> {
        log.debug("getUserByIdentity (actor: $actor, correlationId: $correlationId)...")

        return gettingUserByIdentity.getByIdentity(identity)
    }

    fun getUserByIdentity(
        actor: SystemActor,
        identity: UserIdentity
    ): Either<Error, User> {
        log.debug("getUserByIdentity (actor: $actor)...")

        return gettingUserByIdentity.getByIdentity(identity)
    }
}