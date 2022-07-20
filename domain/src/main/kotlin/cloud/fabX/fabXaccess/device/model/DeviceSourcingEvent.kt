package cloud.fabX.fabXaccess.device.model

import arrow.core.Option
import cloud.fabX.fabXaccess.common.model.ActorId
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.DeviceId
import cloud.fabX.fabXaccess.common.model.SourcingEvent
import cloud.fabX.fabXaccess.common.model.ToolId
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

sealed class DeviceSourcingEvent(
    override val aggregateRootId: DeviceId,
    override val aggregateVersion: Long,
    override val actorId: ActorId,
    override val correlationId: CorrelationId,
    override val timestamp: Instant = Clock.System.now()
) : SourcingEvent {

    abstract fun processBy(eventHandler: EventHandler, device: Option<Device>): Option<Device>

    interface EventHandler {
        fun handle(event: DeviceCreated, device: Option<Device>): Option<Device>
        fun handle(event: DeviceDetailsChanged, device: Option<Device>): Option<Device>
        fun handle(event: ToolAttached, device: Option<Device>): Option<Device>
        fun handle(event: ToolDetached, device: Option<Device>): Option<Device>
        fun handle(event: DeviceDeleted, device: Option<Device>): Option<Device>
    }
}

data class DeviceCreated(
    override val aggregateRootId: DeviceId,
    override val actorId: ActorId,
    override val correlationId: CorrelationId,
    val name: String,
    val background: String,
    val backupBackendUrl: String,
    val mac: String,
    val secret: String
) : DeviceSourcingEvent(aggregateRootId, 1, actorId, correlationId) {

    override fun processBy(eventHandler: EventHandler, device: Option<Device>): Option<Device> =
        eventHandler.handle(this, device)
}

data class DeviceDetailsChanged(
    override val aggregateRootId: DeviceId,
    override val aggregateVersion: Long,
    override val actorId: ActorId,
    override val correlationId: CorrelationId,
    val name: ChangeableValue<String>,
    val background: ChangeableValue<String>,
    val backupBackendUrl: ChangeableValue<String>
) : DeviceSourcingEvent(aggregateRootId, aggregateVersion, actorId, correlationId) {

    override fun processBy(eventHandler: EventHandler, device: Option<Device>): Option<Device> =
        eventHandler.handle(this, device)
}

data class ToolAttached(
    override val aggregateRootId: DeviceId,
    override val aggregateVersion: Long,
    override val actorId: ActorId,
    override val correlationId: CorrelationId,
    val pin: Int,
    val toolId: ToolId
) : DeviceSourcingEvent(aggregateRootId, aggregateVersion, actorId, correlationId) {

    override fun processBy(eventHandler: EventHandler, device: Option<Device>): Option<Device> =
        eventHandler.handle(this, device)
}

data class ToolDetached(
    override val aggregateRootId: DeviceId,
    override val aggregateVersion: Long,
    override val actorId: ActorId,
    override val correlationId: CorrelationId,
    val pin: Int
) : DeviceSourcingEvent(aggregateRootId, aggregateVersion, actorId, correlationId) {

    override fun processBy(eventHandler: EventHandler, device: Option<Device>): Option<Device> =
        eventHandler.handle(this, device)
}

data class DeviceDeleted(
    override val aggregateRootId: DeviceId,
    override val aggregateVersion: Long,
    override val actorId: ActorId,
    override val correlationId: CorrelationId
) : DeviceSourcingEvent(aggregateRootId, aggregateVersion, actorId, correlationId) {

    override fun processBy(eventHandler: EventHandler, device: Option<Device>): Option<Device> =
        eventHandler.handle(this, device)
}