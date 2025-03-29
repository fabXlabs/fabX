package cloud.fabX.fabXaccess.user.application

import arrow.core.Either
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.user.model.Admin
import cloud.fabX.fabXaccess.user.model.Instructor
import cloud.fabX.fabXaccess.user.model.LimitedUser
import cloud.fabX.fabXaccess.user.model.Member
import cloud.fabX.fabXaccess.user.model.User
import cloud.fabX.fabXaccess.user.model.UserRepository
import cloud.fabX.fabXaccess.user.model.toLimitedUser


/**
 * Service to get users.
 */
class GettingUser(
    loggerFactory: LoggerFactory,
    private val userRepository: UserRepository
) {
    private val log: Logger = loggerFactory.invoke(this::class.java)

    suspend fun getAll(
        actor: Admin,
        correlationId: CorrelationId
    ): Set<User> {
        log.debug("getAll (actor: $actor, correlationId: $correlationId)...")

        return userRepository.getAll()
    }

    suspend fun getAllLimited(
        actor: Instructor,
        correlationId: CorrelationId
    ): Set<LimitedUser> {
        log.debug("getAllLimited (actor: $actor, correlationId: $correlationId)...")

        return userRepository.getAll().map { it.toLimitedUser() }.toSet()
    }

    suspend fun getById(
        actor: Admin,
        correlationId: CorrelationId,
        userId: UserId
    ): Either<Error, User> =
        log.logError(actor, correlationId, "getById") {
            userRepository.getById(userId)
        }

    suspend fun getMe(
        actor: Member,
        correlationId: CorrelationId
    ): Either<Error, User> =
        log.logError(actor, correlationId, "getMe") {
            userRepository.getById(actor.userId)
        }
}