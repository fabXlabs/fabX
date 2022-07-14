package cloud.fabX.fabXaccess.device.model

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.model.AggregateRootEntity
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.assertAggregateVersionIncreasesOneByOne
import cloud.fabX.fabXaccess.common.model.assertAggregateVersionStartsWithOne
import cloud.fabX.fabXaccess.common.model.assertIsNotEmpty
import cloud.fabX.fabXaccess.user.model.Admin

data class Device internal constructor(
    override val id: DeviceId,
    override val aggregateVersion: Long,
    val name: String,
    val background: String,
    val backupBackendUrl: String,
    internal val identity: MacSecretIdentity
) : AggregateRootEntity<DeviceId> {

    companion object {
        fun addNew(
            actor: Admin,
            name: String,
            background: String,
            backupBackendUrl: String,
            identity: MacSecretIdentity
        ): DeviceSourcingEvent {
            return DeviceCreated(
                DomainModule.deviceIdFactory().invoke(),
                actor.id,
                name,
                background,
                backupBackendUrl,
                identity.mac,
                identity.secret
            )
        }

        fun fromSourcingEvents(events: Iterable<DeviceSourcingEvent>): Option<Device> {
            events.assertIsNotEmpty()
            events.assertAggregateVersionStartsWithOne()
            events.assertAggregateVersionIncreasesOneByOne()

            val deviceCreatedEvent = events.first()

            if (deviceCreatedEvent !is DeviceCreated) {
                throw EventHistoryDoesNotStartWithDeviceCreated(
                    "Event history starts with ${deviceCreatedEvent}, not a DeviceCreated event."
                )
            }

            return events.fold(None) { result: Option<Device>, event ->
                event.processBy(DeviceEventHandler(), result)
            }
        }
    }

    fun apply(sourcingEvent: DeviceSourcingEvent): Option<Device> =
        sourcingEvent.processBy(DeviceEventHandler(), Some(this))

    fun changeDetails(
        actor: Admin,
        name: ChangeableValue<String> = ChangeableValue.LeaveAsIs,
        background: ChangeableValue<String> = ChangeableValue.LeaveAsIs,
        backupBackendUrl: ChangeableValue<String> = ChangeableValue.LeaveAsIs
    ): DeviceSourcingEvent {
        return DeviceDetailsChanged(
            id,
            aggregateVersion + 1,
            actor.id,
            name,
            background,
            backupBackendUrl
        )
    }

    fun delete(
        actor: Admin
    ): DeviceSourcingEvent {
        return DeviceDeleted(
            id,
            aggregateVersion + 1,
            actor.id
        )
    }

    class EventHistoryDoesNotStartWithDeviceCreated(message: String) : Exception(message)
}