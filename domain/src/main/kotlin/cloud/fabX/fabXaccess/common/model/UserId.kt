package cloud.fabX.fabXaccess.common.model

import java.util.UUID

/**
 * Technical (artificial) ID of a User.
 */
data class UserId(override val value: UUID) : EntityId<UUID>, ActorId {
    companion object {
        fun fromString(s: String): UserId {
            return UserId(UUID.fromString(s))
        }
    }

    fun serialize(): String {
        return value.toString()
    }
}

typealias UserIdFactory = () -> UserId

/**
 * Returns a new UserId.
 *
 * @return a UserId of a random UUID.
 */
fun newUserId(): UserId {
    return UserId(UUID.randomUUID())
}