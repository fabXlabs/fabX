package cloud.fabX.fabXaccess.user.application

import arrow.core.Either
import arrow.core.Option
import arrow.core.flatMap
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.user.model.Admin
import cloud.fabX.fabXaccess.user.model.HardDeletingUser
import cloud.fabX.fabXaccess.user.model.UserRepository
import kotlinx.datetime.Clock

/**
 * Service to handle deleting a user.
 */
class DeletingUser(
    loggerFactory: LoggerFactory,
    private val userRepository: UserRepository,
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
    ): Option<Error> {
        log.debug("deleteUser...")

        return userRepository.getById(userId)
            .flatMap {
                it.delete(actor, clock, correlationId)
            }
            .flatMap {
                userRepository.store(it)
                    .toEither { }
                    .swap()
            }
            .swap()
            .orNone()
            .tapNone { log.debug("...deleteUser done") }
            .tap { log.error("...deleteUser error: $it") }
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
    ): Either<Error, Unit> {
        log.debug("hardDeleteUser (actor: $actor, correlationId: $correlationId)...")

        return hardDeletingUser.hardDelete(userId)
            .map { }
            .tap { log.debug("...hardDeleteUser done") }
            .tapLeft { log.error("...hardDeleteUser error: $it") }
    }
}