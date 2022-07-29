package cloud.fabX.fabXaccess.common.model

import cloud.fabX.fabXaccess.common.application.UuidSerializer
import java.util.UUID
import kotlinx.serialization.Serializable

/**
 * Technical (artificial) ID of a Tool.
 */
@Serializable
data class ToolId(
    @Serializable(with = UuidSerializer::class) override val value: UUID
) : EntityId<UUID> {
    companion object {
        fun fromString(s: String): ToolId {
            return ToolId(UUID.fromString(s))
        }
    }

    fun serialize(): String {
        return value.toString()
    }
}

typealias ToolIdFactory = () -> ToolId

/**
 * Returns a new ToolId.
 *
 * @return a ToolId of a random UUID
 */
fun newToolId(): ToolId {
    return ToolId(UUID.randomUUID())
}