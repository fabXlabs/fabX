package cloud.fabX.fabXaccess.device.infrastructure

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.toOption
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.device.model.Device
import cloud.fabX.fabXaccess.device.model.DeviceId
import cloud.fabX.fabXaccess.device.model.DeviceIdentity
import cloud.fabX.fabXaccess.device.model.DeviceRepository
import cloud.fabX.fabXaccess.device.model.DeviceSourcingEvent
import cloud.fabX.fabXaccess.device.model.GettingDeviceByIdentity
import cloud.fabX.fabXaccess.device.model.GettingDevicesByTool
import cloud.fabX.fabXaccess.tool.model.ToolId

class DeviceDatabaseRepository : DeviceRepository, GettingDeviceByIdentity, GettingDevicesByTool {
    private val events = mutableListOf<DeviceSourcingEvent>()

    override fun getAll(): Set<Device> {
        return events
            .sortedBy { it.aggregateVersion }
            .groupBy { it.aggregateRootId }
            .map { Device.fromSourcingEvents(it.value) }
            .filter { it.isDefined() }
            .map { it.getOrElse { throw IllegalStateException("Is filtered for defined elements.") } }
            .toSet()
    }

    override fun getById(id: DeviceId): Either<Error, Device> {
        val e = events
            .filter { it.aggregateRootId == id }
            .sortedBy { it.aggregateVersion }

        return if (e.isNotEmpty()) {
            Device.fromSourcingEvents(e)
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
        val previousVersion = getVersionById(event.aggregateRootId)

        return if (previousVersion != null
            && event.aggregateVersion != previousVersion + 1
        ) {
            Some(
                Error.VersionConflict(
                    "Previous version of device ${event.aggregateRootId} is $previousVersion, " +
                            "desired new version is ${event.aggregateVersion}."
                )
            )
        } else {
            events.add(event)
            None
        }
    }

    private fun getVersionById(id: DeviceId): Long? {
        return events
            .filter { it.aggregateRootId == id }
            .maxOfOrNull { it.aggregateVersion }
    }

    override fun getByIdentity(identity: DeviceIdentity): Either<Error, Device> =
        getAll()
            .firstOrNull { it.hasIdentity(identity) }
            .toOption()
            .toEither { Error.DeviceNotFoundByIdentity("Not able to find device for given identity.") }

    override fun getByTool(toolId: ToolId): Set<Device> {
        return getAll()
            .filter { it.hasAttachedTool(toolId) }
            .toSet()
    }
}