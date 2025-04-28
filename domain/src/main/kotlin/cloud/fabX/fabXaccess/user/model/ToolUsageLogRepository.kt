package cloud.fabX.fabXaccess.user.model

import arrow.core.Either
import cloud.fabX.fabXaccess.common.model.Error

interface ToolUsageLogRepository {
    suspend fun getAll(): List<ToolUsageLogEntry>
    suspend fun store(toolUsage: ToolUsageLogEntry): Either<Error, Unit>
}
