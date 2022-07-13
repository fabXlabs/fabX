package cloud.fabX.fabXaccess.user.model

import cloud.fabX.fabXaccess.common.model.ActorId
import cloud.fabX.fabXaccess.common.model.EntityId
import java.util.UUID

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