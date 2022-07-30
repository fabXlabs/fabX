package cloud.fabX.fabXaccess.device.infrastructure

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.toOption
import cloud.fabX.fabXaccess.common.jsonb
import cloud.fabX.fabXaccess.common.model.DeviceId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.ToolId
import cloud.fabX.fabXaccess.device.model.Device
import cloud.fabX.fabXaccess.device.model.DeviceIdentity
import cloud.fabX.fabXaccess.device.model.DeviceRepository
import cloud.fabX.fabXaccess.device.model.DeviceSourcingEvent
import cloud.fabX.fabXaccess.device.model.GettingDeviceByIdentity
import cloud.fabX.fabXaccess.device.model.GettingDevicesByAttachedTool
import kotlinx.datetime.toJavaInstant
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object DeviceSourcingEventDAO : Table("DeviceSourcingEvent") {
    val aggregateRootId = uuid("aggregate_root_id")
    val aggregateVersion = long("aggregate_version")
    val actorId = uuid("actor_id")
    val correlationId = uuid("correlation_id")
    val timestamp = timestamp("timestamp")
    val data = jsonb("data", DeviceSourcingEvent.serializer())
}

class DeviceDatabaseRepository(
    private val db: Database
) : DeviceRepository, GettingDeviceByIdentity, GettingDevicesByAttachedTool {

    override fun getAll(): Set<Device> {
        return transaction {
            DeviceSourcingEventDAO
                .selectAll()
                .orderBy(DeviceSourcingEventDAO.aggregateVersion, order = SortOrder.ASC)
                .asSequence()
                .map {
                    it[DeviceSourcingEventDAO.data]
                }
                .groupBy { it.aggregateRootId }
                .map { Device.fromSourcingEvents(it.value) }
                .filter { it.isDefined() }
                .map { it.getOrElse { throw IllegalStateException("Is filtered for defined elements.") } }
                .toSet()
        }
    }

    override fun getById(id: DeviceId): Either<Error, Device> {
        val events = transaction {
            DeviceSourcingEventDAO
                .select {
                    DeviceSourcingEventDAO.aggregateRootId.eq(id.value)
                }
                .orderBy(DeviceSourcingEventDAO.aggregateVersion)
                .map {
                    it[DeviceSourcingEventDAO.data]
                }
        }

        return if (events.isNotEmpty()) {
            Device.fromSourcingEvents(events)
                .toEither {
                    Error.DeviceNotFound(
                        "Device with id $id not found.",
                        id
                    )
                }
        } else {
            Error.DeviceNotFound(
                "Device with id $id not found.",
                id
            ).left()
        }
    }

    override fun store(event: DeviceSourcingEvent): Option<Error> {
        return transaction {
            val previousVersion = getVersionById(event.aggregateRootId)

            if (previousVersion != null
                && event.aggregateVersion != previousVersion + 1
            ) {
                Some(
                    Error.VersionConflict(
                        "Previous version of device ${event.aggregateRootId} is $previousVersion, " +
                                "desired new version is ${event.aggregateVersion}."
                    )
                )
            } else {
                DeviceSourcingEventDAO.insert {
                    it[aggregateRootId] = event.aggregateRootId.value
                    it[aggregateVersion] = event.aggregateVersion
                    it[actorId] = event.actorId.value
                    it[correlationId] = event.correlationId.id
                    it[timestamp] = event.timestamp.toJavaInstant()
                    it[data] = event
                }

                None
            }
        }
    }

    fun getSourcingEvents(): List<DeviceSourcingEvent> {
        return transaction {
            DeviceSourcingEventDAO.selectAll()
                .orderBy(DeviceSourcingEventDAO.timestamp, SortOrder.ASC)
                .orderBy(DeviceSourcingEventDAO.aggregateVersion, SortOrder.ASC)
                .map {
                    it[DeviceSourcingEventDAO.data]
                }
        }
    }

    @Suppress("unused") // supposed to be executed within Transaction
    private fun Transaction.getVersionById(id: DeviceId): Long? {
        return DeviceSourcingEventDAO
            .slice(DeviceSourcingEventDAO.aggregateVersion)
            .select {
                DeviceSourcingEventDAO.aggregateRootId.eq(id.value)
            }
            .orderBy(DeviceSourcingEventDAO.aggregateVersion, order = SortOrder.DESC)
            .limit(1)
            .map { it[DeviceSourcingEventDAO.aggregateVersion] }
            .maxOfOrNull { it }
    }

    override fun getByIdentity(identity: DeviceIdentity): Either<Error, Device> =
        getAll()
            .firstOrNull { it.hasIdentity(identity) }
            .toOption()
            .toEither { Error.DeviceNotFoundByIdentity("Not able to find device for given identity.") }

    override fun getByAttachedTool(toolId: ToolId): Set<Device> =
        getAll()
            .filter { it.hasAttachedTool(toolId) }
            .toSet()

    private fun <T> transaction(statement: Transaction.() -> T): T = transaction(db) {
        statement()
    }
}