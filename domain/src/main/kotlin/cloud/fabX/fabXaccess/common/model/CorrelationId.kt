package cloud.fabX.fabXaccess.common.model

import java.util.UUID

/**
 * ID to correlate events related to a single request.
 */
data class CorrelationId(val id: UUID)

typealias CorrelationIdFactory = () -> CorrelationId

fun newCorrelationId(): CorrelationId {
    return CorrelationId(UUID.randomUUID())
}