package cloud.fabX.fabXaccess.user.infrastructure

import arrow.core.Either
import arrow.core.right
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.ToolId
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.user.model.ToolUsageLogEntry
import cloud.fabX.fabXaccess.user.model.ToolUsageLogRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object ToolUsageLogDAO : Table("ToolUsageLog") {
    val timestamp = timestamp("timestamp")
    val userId = uuid("user_id")
    val toolId = uuid("tool_id")
}

class ToolUsageLogDatabaseRepository(private val db: Database) : ToolUsageLogRepository {
    override suspend fun getAll(): List<ToolUsageLogEntry> {
        return transaction {
            ToolUsageLogDAO
                .selectAll()
                .orderBy(ToolUsageLogDAO.timestamp)
                .asSequence()
                .map {
                    ToolUsageLogEntry(
                        it[ToolUsageLogDAO.timestamp].toKotlinInstant(),
                        UserId(it[ToolUsageLogDAO.userId]),
                        ToolId(it[ToolUsageLogDAO.toolId])
                    )
                }
                .toList()
        }
    }

    override suspend fun store(toolUsage: ToolUsageLogEntry): Either<Error, Unit> {
        return transaction {
            ToolUsageLogDAO.insert {
                it[ToolUsageLogDAO.timestamp] = toolUsage.timestamp.toJavaInstant()
                it[ToolUsageLogDAO.userId] = toolUsage.userId.value
                it[ToolUsageLogDAO.toolId] = toolUsage.toolId.value
            }
            Unit.right()
        }
    }

    private suspend fun <T> transaction(statement: Transaction.() -> T): T = withContext(Dispatchers.IO) {
        transaction(db) {
            statement()
        }
    }
}
