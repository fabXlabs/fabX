package cloud.fabX.fabXaccess.tool.infrastructure

import arrow.core.Either
import arrow.core.Option
import arrow.core.left
import arrow.core.right
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.ToolId
import cloud.fabX.fabXaccess.tool.model.Tool
import cloud.fabX.fabXaccess.tool.model.ToolSourcingEvent
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jetbrains.exposed.sql.Database

class CachedToolDatabaseRepository(
    loggerFactory: LoggerFactory,
    db: Database
) : ToolDatabaseRepository(loggerFactory, db) {
    private val cacheMutex = Mutex()

    private var cache: MutableMap<ToolId, Tool>? = null
    private val outdated: MutableSet<ToolId> = mutableSetOf()

    override suspend fun getAll(): Set<Tool> {
        cacheMutex.withLock {
            if (cache == null) {
                cache = super.getAll().associateBy { it.id }.toMutableMap()
            }
            if (outdated.size <= 1) {
                outdated.forEach { id ->
                    super.getById(id)
                        .onRight { cache!![id] = it }
                }
            } else {
                cache = super.getAll().associateBy { it.id }.toMutableMap()
            }
            outdated.clear()

            return cache!!.values.toSet()
        }
    }

    override suspend fun getById(id: ToolId): Either<Error, Tool> {
        cacheMutex.withLock {
            if (cache == null) {
                cache = super.getAll().associateBy { it.id }.toMutableMap()
            } else if (outdated.contains(id)) {
                super.getById(id)
                    .onRight { cache!![id] = it }
                outdated.remove(id)
            }

            return cache!![id]?.right()
                ?: Error.ToolNotFound(
                    "Tool with id $id not found.",
                    id
                ).left()
        }
    }

    override suspend fun store(event: ToolSourcingEvent): Option<Error> {
        cacheMutex.withLock {
            outdated.add(event.aggregateRootId)
            cache?.remove(event.aggregateRootId)
        }
        return super.store(event)
    }
}