package cloud.fabX.fabXaccess.device.infrastructure

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.left
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.device.model.Device
import cloud.fabX.fabXaccess.device.model.DeviceId
import cloud.fabX.fabXaccess.device.model.DeviceRepository
import cloud.fabX.fabXaccess.device.model.DeviceSourcingEvent

class DeviceDatabaseRepository : DeviceRepository {
    private val events = mutableListOf<DeviceSourcingEvent>()

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
}