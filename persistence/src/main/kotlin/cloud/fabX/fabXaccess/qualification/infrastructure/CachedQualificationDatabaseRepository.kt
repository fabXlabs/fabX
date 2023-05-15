package cloud.fabX.fabXaccess.qualification.infrastructure

import arrow.core.Either
import arrow.core.Option
import arrow.core.left
import arrow.core.right
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.QualificationId
import cloud.fabX.fabXaccess.qualification.model.Qualification
import cloud.fabX.fabXaccess.qualification.model.QualificationSourcingEvent
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jetbrains.exposed.sql.Database

class CachedQualificationDatabaseRepository(db: Database) : QualificationDatabaseRepository(db) {

    private val cacheMutex = Mutex()

    private var cache: MutableMap<QualificationId, Qualification>? = null
    private val outdated: MutableSet<QualificationId> = mutableSetOf()

    override suspend fun getAll(): Set<Qualification> {
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
        }
        return cache!!.values.toSet()
    }

    override suspend fun getById(id: QualificationId): Either<Error, Qualification> {
        cacheMutex.withLock {
            if (cache == null) {
                cache = super.getAll().associateBy { it.id }.toMutableMap()
            } else if (outdated.contains(id)) {
                super.getById(id)
                    .onRight { cache!![id] = it }
                outdated.remove(id)
            }
        }
        return cache!![id]?.right()
            ?: Error.QualificationNotFound(
                "Qualification with id $id not found.",
                id
            ).left()
    }

    override suspend fun store(event: QualificationSourcingEvent): Option<Error> {
        cacheMutex.withLock {
            outdated.add(event.aggregateRootId)
            cache?.remove(event.aggregateRootId)
        }
        return super.store(event)
    }
}