package cloud.fabX.fabXaccess.device.model

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.getOrElse
import cloud.fabX.fabXaccess.common.model.valueToChangeTo

internal class DeviceEventHandler : DeviceSourcingEvent.EventHandler {

    override fun handle(event: DeviceCreated, device: Option<Device>): Option<Device> {
        if (device.isSome()) {
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
                backupBackendUrl = event.backupBackendUrl,
                actualFirmwareVersion = null,
                desiredFirmwareVersion = null,
                identity = MacSecretIdentity(event.mac, event.secret),
                attachedTools = mapOf()
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
        event: ActualFirmwareVersionChanged,
        device: Option<Device>
    ): Option<Device> = requireSomeDeviceWithSameIdAnd(event, device) { e, d ->
        Some(
            d.copy(
                aggregateVersion = e.aggregateVersion,
                actualFirmwareVersion = e.actualFirmwareVersion
            )
        )
    }

    override fun handle(
        event: DesiredFirmwareVersionChanged,
        device: Option<Device>
    ): Option<Device> = requireSomeDeviceWithSameIdAnd(event, device) { e, d ->
        Some(
            d.copy(
                aggregateVersion = e.aggregateVersion,
                desiredFirmwareVersion = e.desiredFirmwareVersion
            )
        )
    }

    override fun handle(
        event: ToolAttached,
        device: Option<Device>
    ): Option<Device> = requireSomeDeviceWithSameIdAnd(event, device) { e, d ->
        Some(
            d.copy(
                aggregateVersion = e.aggregateVersion,
                attachedTools = d.attachedTools + (e.pin to e.toolId)
            )
        )
    }

    override fun handle(
        event: ToolDetached,
        device: Option<Device>
    ): Option<Device> = requireSomeDeviceWithSameIdAnd(event, device) { e, d ->
        Some(
            d.copy(
                aggregateVersion = e.aggregateVersion,
                attachedTools = d.attachedTools - e.pin
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