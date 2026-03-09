package cloud.fabX.fabXaccess.user.infrastructure

import arrow.core.Either
import arrow.core.right
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.user.model.ToolUsageLogEntry
import cloud.fabX.fabXaccess.user.model.ToolUsageLogRepository

class ToolUsageLogNoopRepository : ToolUsageLogRepository {
    override suspend fun getAll(): List<ToolUsageLogEntry> {
        return emptyList()
    }

    override suspend fun store(toolUsage: ToolUsageLogEntry): Either<Error, Unit> {
        return Unit.right()
    }
}
