package cloud.fabX.fabXaccess.device.infrastructure

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import arrow.core.toOption
import cloud.fabX.fabXaccess.common.application.domainSerializersModule
import cloud.fabX.fabXaccess.common.model.ActorId
import cloud.fabX.fabXaccess.common.model.DeviceId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.ToolId
import cloud.fabX.fabXaccess.device.model.Device
import cloud.fabX.fabXaccess.device.model.DeviceIdentity
import cloud.fabX.fabXaccess.device.model.DeviceRepository
import cloud.fabX.fabXaccess.device.model.DeviceSourcingEvent
import cloud.fabX.fabXaccess.device.model.GettingDeviceByIdentity
import cloud.fabX.fabXaccess.device.model.GettingDevicesByAttachedTool
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.toJavaInstant
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.json.jsonb
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.upsert

object DeviceSourcingEventDAO : Table("DeviceSourcingEvent") {
    val aggregateRootId = uuid("aggregate_root_id")
    val aggregateVersion = long("aggregate_version")
    val actorId = uuid("actor_id")
    val correlationId = uuid("correlation_id")
    val timestamp = timestamp("timestamp")
    val data = jsonb<DeviceSourcingEvent>("data", Json { serializersModule = domainSerializersModule })
}

object DeviceThumbnailDAO : Table("DeviceThumbnail") {
    val aggregateRootId = uuid("aggregate_root_id").uniqueIndex()
    val actorId = uuid("actor_id")
    val thumbnailData = blob("thumbnail_data")

    override val primaryKey = PrimaryKey(aggregateRootId, name = "aggregate_root_id_pk")
}

open class DeviceDatabaseRepository(
    private val db: Database
) : DeviceRepository, GettingDeviceByIdentity, GettingDevicesByAttachedTool {

    override suspend fun getAll(): Set<Device> {
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
                .filter { it.isSome() }
                .map { it.getOrElse { throw IllegalStateException("Is filtered for defined elements.") } }
                .toSet()
        }
    }

    override suspend fun getById(id: DeviceId): Either<Error, Device> {
        val events = transaction {
            DeviceSourcingEventDAO
                .selectAll()
                .where { DeviceSourcingEventDAO.aggregateRootId.eq(id.value) }
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

    override suspend fun store(event: DeviceSourcingEvent): Either<Error, Unit> {
        return transaction {
            val previousVersion = getVersionById(event.aggregateRootId)

            if (previousVersion != null
                && event.aggregateVersion != previousVersion + 1
            ) {
                Error.VersionConflict(
                    "Previous version of device ${event.aggregateRootId} is $previousVersion, " +
                            "desired new version is ${event.aggregateVersion}."
                ).left()
            } else {
                DeviceSourcingEventDAO.insert {
                    it[aggregateRootId] = event.aggregateRootId.value
                    it[aggregateVersion] = event.aggregateVersion
                    it[actorId] = event.actorId.value
                    it[correlationId] = event.correlationId.id
                    it[timestamp] = event.timestamp.toJavaInstant()
                    it[data] = event
                }

                Unit.right()
            }
        }
    }

    override suspend fun storeThumbnail(
        id: DeviceId,
        actor: ActorId,
        thumbnail: ByteArray
    ): Either<Error, Unit> {
        return transaction {
            DeviceThumbnailDAO.upsert {
                it[aggregateRootId] = id.value
                it[actorId] = actor.value
                it[thumbnailData] = ExposedBlob(thumbnail)
            }
            Unit.right()
        }
    }

    override suspend fun getThumbnail(id: DeviceId): Either<Error, ByteArray> {
        return transaction {
            DeviceThumbnailDAO.select(DeviceThumbnailDAO.thumbnailData)
                .where { DeviceThumbnailDAO.aggregateRootId eq id.value }
                .map {
                    it[DeviceThumbnailDAO.thumbnailData].bytes
                }
                .firstOrNull()
                .toOption()
                .toEither {
                    Error.DeviceThumbnailNotFound("No thumbnail for Device with id $id found.", id)
                }
        }
    }

    suspend fun getSourcingEvents(): List<DeviceSourcingEvent> {
        return transaction {
            DeviceSourcingEventDAO.selectAll()
                .orderBy(DeviceSourcingEventDAO.timestamp, SortOrder.ASC)
                .orderBy(DeviceSourcingEventDAO.aggregateVersion, SortOrder.ASC)
                .map {
                    it[DeviceSourcingEventDAO.data]
                }
        }
    }

    @Suppress("UnusedReceiverParameter") // supposed to be executed within Transaction
    private fun Transaction.getVersionById(id: DeviceId): Long? {
        return DeviceSourcingEventDAO
            .select(DeviceSourcingEventDAO.aggregateVersion)
            .where { DeviceSourcingEventDAO.aggregateRootId.eq(id.value) }
            .orderBy(DeviceSourcingEventDAO.aggregateVersion, order = SortOrder.DESC)
            .limit(1)
            .map { it[DeviceSourcingEventDAO.aggregateVersion] }
            .maxOfOrNull { it }
    }

    override suspend fun getByIdentity(identity: DeviceIdentity): Either<Error, Device> =
        getAll()
            .firstOrNull { it.hasIdentity(identity) }
            .toOption()
            .toEither { Error.DeviceNotFoundByIdentity("Not able to find device for given identity.") }

    override suspend fun getByAttachedTool(toolId: ToolId): Set<Device> =
        getAll()
            .filter { it.hasAttachedTool(toolId) }
            .toSet()

    private suspend fun <T> transaction(statement: Transaction.() -> T): T = withContext(Dispatchers.IO) {
        transaction(db) {
            statement()
        }
    }
}