package cloud.fabX.fabXaccess.user.model

import cloud.fabX.fabXaccess.common.model.ActorId
import cloud.fabX.fabXaccess.common.model.EntityId
import java.util.UUID

// TODO move Ids to common package (disallow importing aggregate2.model.* in aggregate1.model.*)?

/**
 * Technical (artificial) ID of a User.
 */
data class UserId(override val value: UUID) : EntityId<UUID>, ActorId

typealias UserIdFactory = () -> UserId

/**
 * Returns a new UserId.
 *
 * @return a UserId of a random UUID.
 */
fun newUserId(): UserId {
    return UserId(UUID.randomUUID())
}