package cloud.fabX.fabXaccess.common.model

import cloud.fabX.fabXaccess.common.application.UuidSerializer
import java.util.UUID
import kotlinx.serialization.Serializable

/**
 * Technical (artificial) ID of a User.
 */
@Serializable
data class UserId(
    @Serializable(with = UuidSerializer::class) override val value: UUID
) : EntityId<UUID>, ActorId {
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