package cloud.fabX.fabXaccess.device.model

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.getOrElse
import cloud.fabX.fabXaccess.common.model.AggregateRootEntity
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.assertAggregateVersionIncreasesOneByOne
import cloud.fabX.fabXaccess.common.model.assertAggregateVersionStartsWithOne
import cloud.fabX.fabXaccess.common.model.assertIsNotEmpty
import cloud.fabX.fabXaccess.common.model.valueToChangeTo
import cloud.fabX.fabXaccess.user.model.Admin

// TODO DeviceMacSecretIdentification

data class Device internal constructor(
    override val id: DeviceId,
    override val aggregateVersion: Long,
    val name: String,
    val background: String,
    val backupBackendUrl: String
) : AggregateRootEntity<DeviceId> {

    companion object {
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
                event.processBy(EventHandler(), result)
            }
        }
    }

    fun apply(sourcingEvent: DeviceSourcingEvent): Option<Device> =
        sourcingEvent.processBy(EventHandler(), Some(this))

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

    private class EventHandler : DeviceSourcingEvent.EventHandler {
        override fun handle(event: DeviceCreated, device: Option<Device>): Option<Device> {
            if (device.isDefined()) {
                throw AccumulatorNotEmptyForDeviceCreatedEventHandler(
                    "Handler for DeviceCreated is given $device."
                )
            }

            return Some(
                Device(
                    id = event.aggregateRootId,
                    aggregateVersion = event.aggregateVersion,
                    name = event.name,
                    background = event.background,
                    backupBackendUrl = event.backupBackendUrl
                )
            )
        }

        override fun handle(
            event: DeviceDetailsChanged,
            device: Option<Device>
        ): Option<Device> = requireSomeDeviceWithSameIdAnd(event, device) { e, d ->
            Some(
                d.copy(
                    aggregateVersion = e.aggregateVersion,
                    name = e.name.valueToChangeTo(d.name),
                    background = e.background.valueToChangeTo(d.background),
                    backupBackendUrl = e.backupBackendUrl.valueToChangeTo(d.backupBackendUrl)
                )
            )
        }

        override fun handle(
            event: DeviceDeleted,
            device: Option<Device>
        ): Option<Device> = requireSomeDeviceWithSameIdAnd(event, device) { _, _ ->
            None
        }

        private fun <E : DeviceSourcingEvent> requireSomeDeviceWithSameIdAnd(
            event: E,
            device: Option<Device>,
            and: (E, Device) -> Option<Device>
        ): Option<Device> {
            if (device.map { it.id != event.aggregateRootId }.getOrElse { false }) {
                throw EventAggregateRootIdDoesNotMatchDeviceId(
                    "Event $event cannot be applied to $device. Aggregate root id does not match."
                )
            }

            return device.flatMap { and(event, it) }
        }

        class EventAggregateRootIdDoesNotMatchDeviceId(message: String) : Exception(message)
        class AccumulatorNotEmptyForDeviceCreatedEventHandler(message: String) : Exception(message)
    }

    class EventHistoryDoesNotStartWithDeviceCreated(message: String) : Exception(message)
}