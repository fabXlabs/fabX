package cloud.fabX.fabXaccess.common.model

import cloud.fabX.fabXaccess.common.application.UuidSerializer
import java.util.UUID
import kotlinx.serialization.Serializable

/**
 * ID to correlate events related to a single request.
 */
@Serializable
data class CorrelationId(
    @Serializable(with = UuidSerializer::class) val id: UUID
) {
    companion object {
        fun fromString(s: String): CorrelationId {
            return CorrelationId(UUID.fromString(s))
        }
    }

    fun serialize(): String {
        return id.toString()
    }
}

fun newCorrelationId(): CorrelationId {
    return CorrelationId(UUID.randomUUID())
}