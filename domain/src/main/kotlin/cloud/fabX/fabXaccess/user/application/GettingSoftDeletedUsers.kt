package cloud.fabX.fabXaccess.user.application

import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.user.model.Admin
import cloud.fabX.fabXaccess.user.model.GettingSoftDeletedUsers
import cloud.fabX.fabXaccess.user.model.User

/**
 * Service to get users which are soft deleted (i.e., deleted by sourcing event).
 *
 * Allows administrators to then hard delete users (i.e., to delete all sourcing events regarding the user)
 * to comply with data protection regulations.
 */
class GettingSoftDeletedUsers(
    loggerFactory: LoggerFactory,
    private val gettingSoftDeletedUsers: GettingSoftDeletedUsers
) {
    private val log: Logger = loggerFactory.invoke(this::class.java)

    suspend fun getSoftDeletedUsers(
        actor: Admin,
        correlationId: CorrelationId,
    ): Set<User> {
        log.debug("getSoftDeletedUsers (actor: $actor, correlationId: $correlationId)...")

        val softDeletedUsers = gettingSoftDeletedUsers.getSoftDeleted()
        log.debug("...getSoftDeletedUsers done")

        return softDeletedUsers
    }
}