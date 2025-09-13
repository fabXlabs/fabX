package cloud.fabX.fabXaccess.user.application

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.user.model.Admin
import cloud.fabX.fabXaccess.user.model.GettingSoftDeletedUsers
import cloud.fabX.fabXaccess.user.model.HardDeletingUser
import cloud.fabX.fabXaccess.user.model.UserRepository
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Service to handle deleting a user.
 */
@OptIn(ExperimentalTime::class)
class DeletingUser(
    loggerFactory: LoggerFactory,
    private val userRepository: UserRepository,
    private val gettingSoftDeletedUsers: GettingSoftDeletedUsers,
    private val hardDeletingUser: HardDeletingUser,
    private val clock: Clock
) {
    private val log: Logger = loggerFactory.invoke(this::class.java)

    /**
     * Soft deletes a user, i.e. writes a user deleted sourcing event to the database.
     *
     * This means that any user data is no longer accessible via the API, but it still exists within the database.
     */
    suspend fun deleteUser(
        actor: Admin,
        correlationId: CorrelationId,
        userId: UserId
    ): Either<Error, Unit> =
        userRepository.getAndStoreFlatMap(userId, actor, correlationId, log, "deleteUser") {
            it.delete(actor, clock, correlationId)
        }

    /**
     * Hard deletes a user, i.e. deletes all sourcing events which concern the given user from the database.
     *
     * This kind of deletion may be required to comply with data protection regulations.
     */
    suspend fun hardDeleteUser(
        actor: Admin,
        correlationId: CorrelationId,
        userId: UserId
    ): Either<Error, Unit> =
        log.logError(actor, correlationId, "hardDeleteUser") {
            requireUserIsSoftDeleted(correlationId, userId)
                .flatMap { hardDeletingUser.hardDelete(userId) }
                .map { }
        }

    private suspend fun requireUserIsSoftDeleted(
        correlationId: CorrelationId,
        userId: UserId
    ): Either<Error, Unit> =
        if (!gettingSoftDeletedUsers.getSoftDeleted().map { it.id }.contains(userId)) {
            Error.SoftDeletedUserNotFound(
                "Soft deleted user not found.",
                userId,
                correlationId
            ).left()
        } else {
            Unit.right()
        }
}