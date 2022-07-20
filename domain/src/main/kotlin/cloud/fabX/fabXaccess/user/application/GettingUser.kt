package cloud.fabX.fabXaccess.user.application

import arrow.core.Either
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.application.logger
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.user.model.Admin
import cloud.fabX.fabXaccess.user.model.User

/**
 * Service to get users.
 */
class GettingUser {

    private val log = logger()
    private val userRepository = DomainModule.userRepository()

    fun getAll(
        actor: Admin,
        correlationId: CorrelationId
    ): Set<User> {
        log.debug("getAll (actor: $actor, correlationId: $correlationId)...")

        return userRepository.getAll()
    }

    fun getById(
        actor: Admin,
        correlationId: CorrelationId,
        userId: UserId
    ): Either<Error, User> {
        log.debug("getById (actor: $actor, correlationId: $correlationId)...")

        return userRepository.getById(userId)
    }
}