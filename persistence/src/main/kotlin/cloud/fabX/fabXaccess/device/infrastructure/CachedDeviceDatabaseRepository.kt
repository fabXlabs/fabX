package cloud.fabX.fabXaccess.device.infrastructure

import arrow.core.Either
import arrow.core.Option
import arrow.core.left
import arrow.core.right
import cloud.fabX.fabXaccess.common.model.DeviceId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.device.model.Device
import cloud.fabX.fabXaccess.device.model.DeviceSourcingEvent
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jetbrains.exposed.sql.Database

class CachedDeviceDatabaseRepository(db: Database) : DeviceDatabaseRepository(db) {

    private val cacheMutex = Mutex()

    private var cache: MutableMap<DeviceId, Device>? = null
    private val outdated: MutableSet<DeviceId> = mutableSetOf()

    override suspend fun getAll(): Set<Device> {
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

    override suspend fun getById(id: DeviceId): Either<Error, Device> {
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
            ?: Error.DeviceNotFound(
                "Device with id $id not found.",
                id
            ).left()
    }

    override suspend fun store(event: DeviceSourcingEvent): Option<Error> {
        cacheMutex.withLock {
            outdated.add(event.aggregateRootId)
            cache?.remove(event.aggregateRootId)
        }
        return super.store(event)
    }
}