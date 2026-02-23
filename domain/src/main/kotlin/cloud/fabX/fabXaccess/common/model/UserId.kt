package cloud.fabX.fabXaccess.common.model

import cloud.fabX.fabXaccess.common.application.UuidSerializer
import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable

/**
 * Technical (artificial) ID of a User.
 */
@Serializable
data class UserId(
    @Serializable(with = UuidSerializer::class) override val value: Uuid
) : EntityId<Uuid>, ActorId {
    companion object {
        fun fromString(s: String): UserId {
            return UserId(Uuid.parseHexDash(s))
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
    return UserId(Uuid.random())
}