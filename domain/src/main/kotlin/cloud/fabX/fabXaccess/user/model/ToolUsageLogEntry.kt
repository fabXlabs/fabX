package cloud.fabX.fabXaccess.user.model

import cloud.fabX.fabXaccess.common.application.withSecondPrecision
import cloud.fabX.fabXaccess.common.model.ToolId
import cloud.fabX.fabXaccess.common.model.UserId
import kotlinx.datetime.Instant

data class ToolUsageLogEntry(
    val timestamp: Instant,
    val userId: UserId,
    val toolId: ToolId
) {
    companion object {
        fun fromUnvalidated(timestamp: Instant, userId: UserId, toolId: ToolId): ToolUsageLogEntry {
            return ToolUsageLogEntry(
                timestamp.withSecondPrecision(),
                userId,
                toolId
            )
        }
    }
}
