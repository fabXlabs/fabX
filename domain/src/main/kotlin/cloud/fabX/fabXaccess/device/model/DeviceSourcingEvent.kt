package cloud.fabX.fabXaccess.device.model

import arrow.core.Option
import cloud.fabX.fabXaccess.common.model.ActorId
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.DeviceId
import cloud.fabX.fabXaccess.common.model.SourcingEvent
import cloud.fabX.fabXaccess.common.model.ToolId
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@OptIn(ExperimentalTime::class)
@Serializable
sealed class DeviceSourcingEvent : SourcingEvent {
    abstract override val aggregateRootId: DeviceId
    abstract override val aggregateVersion: Long
    abstract override val actorId: ActorId
    abstract override val timestamp: Instant
    abstract override val correlationId: CorrelationId

    abstract fun processBy(eventHandler: EventHandler, device: Option<Device>): Option<Device>

    interface EventHandler {
        fun handle(event: DeviceCreated, device: Option<Device>): Option<Device>
        fun handle(event: DeviceDetailsChanged, device: Option<Device>): Option<Device>
        fun handle(event: ActualFirmwareVersionChanged, device: Option<Device>): Option<Device>
        fun handle(event: DesiredFirmwareVersionChanged, device: Option<Device>): Option<Device>
        fun handle(event: ToolAttached, device: Option<Device>): Option<Device>
        fun handle(event: ToolDetached, device: Option<Device>): Option<Device>
        fun handle(event: DeviceDeleted, device: Option<Device>): Option<Device>
    }
}

@OptIn(ExperimentalTime::class)
@Serializable
data class DeviceCreated(
    override val aggregateRootId: DeviceId,
    override val actorId: ActorId,
    override val timestamp: Instant,
    override val correlationId: CorrelationId,
    val name: String,
    val background: String,
    val backupBackendUrl: String,
    val mac: String,
    val secret: String
) : DeviceSourcingEvent() {
    override val aggregateVersion: Long = 1

    override fun processBy(eventHandler: EventHandler, device: Option<Device>): Option<Device> =
        eventHandler.handle(this, device)
}

@OptIn(ExperimentalTime::class)
@Serializable
data class DeviceDetailsChanged(
    override val aggregateRootId: DeviceId,
    override val aggregateVersion: Long,
    override val actorId: ActorId,
    override val timestamp: Instant,
    override val correlationId: CorrelationId,
    val name: ChangeableValue<String>,
    val background: ChangeableValue<String>,
    val backupBackendUrl: ChangeableValue<String>
) : DeviceSourcingEvent() {

    override fun processBy(eventHandler: EventHandler, device: Option<Device>): Option<Device> =
        eventHandler.handle(this, device)
}

@OptIn(ExperimentalTime::class)
@Serializable
data class ActualFirmwareVersionChanged(
    override val aggregateRootId: DeviceId,
    override val aggregateVersion: Long,
    override val actorId: ActorId,
    override val timestamp: Instant,
    override val correlationId: CorrelationId,
    val actualFirmwareVersion: String
) : DeviceSourcingEvent() {

    override fun processBy(eventHandler: EventHandler, device: Option<Device>): Option<Device> =
        eventHandler.handle(this, device)
}

@OptIn(ExperimentalTime::class)
@Serializable
data class DesiredFirmwareVersionChanged(
    override val aggregateRootId: DeviceId,
    override val aggregateVersion: Long,
    override val actorId: ActorId,
    override val timestamp: Instant,
    override val correlationId: CorrelationId,
    val desiredFirmwareVersion: String
) : DeviceSourcingEvent() {

    override fun processBy(eventHandler: EventHandler, device: Option<Device>): Option<Device> =
        eventHandler.handle(this, device)
}

@OptIn(ExperimentalTime::class)
@Serializable
data class ToolAttached(
    override val aggregateRootId: DeviceId,
    override val aggregateVersion: Long,
    override val actorId: ActorId,
    override val timestamp: Instant,
    override val correlationId: CorrelationId,
    val pin: Int,
    val toolId: ToolId
) : DeviceSourcingEvent() {

    override fun processBy(eventHandler: EventHandler, device: Option<Device>): Option<Device> =
        eventHandler.handle(this, device)
}

@OptIn(ExperimentalTime::class)
@Serializable
data class ToolDetached(
    override val aggregateRootId: DeviceId,
    override val aggregateVersion: Long,
    override val actorId: ActorId,
    override val timestamp: Instant,
    override val correlationId: CorrelationId,
    val pin: Int
) : DeviceSourcingEvent() {

    override fun processBy(eventHandler: EventHandler, device: Option<Device>): Option<Device> =
        eventHandler.handle(this, device)
}

@OptIn(ExperimentalTime::class)
@Serializable
data class DeviceDeleted(
    override val aggregateRootId: DeviceId,
    override val aggregateVersion: Long,
    override val actorId: ActorId,
    override val timestamp: Instant,
    override val correlationId: CorrelationId
) : DeviceSourcingEvent() {

    override fun processBy(eventHandler: EventHandler, device: Option<Device>): Option<Device> =
        eventHandler.handle(this, device)
}