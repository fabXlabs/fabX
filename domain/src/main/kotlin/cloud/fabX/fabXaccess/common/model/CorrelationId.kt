package cloud.fabX.fabXaccess.common.model

import cloud.fabX.fabXaccess.common.application.UuidSerializer
import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable

/**
 * ID to correlate events related to a single request.
 */
@Serializable
data class CorrelationId(
    @Serializable(with = UuidSerializer::class) val id: Uuid
) {
    companion object {
        fun fromString(s: String): CorrelationId {
            return CorrelationId(Uuid.parseHexDash(s))
        }
    }

    fun serialize(): String {
        return id.toString()
    }
}

fun newCorrelationId(): CorrelationId {
    return CorrelationId(Uuid.random())
}