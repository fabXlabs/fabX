package cloud.fabX.fabXaccess.device.model

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.flatMap
import arrow.core.getOrNone
import arrow.core.left
import arrow.core.right
import cloud.fabX.fabXaccess.common.model.ActorId
import cloud.fabX.fabXaccess.common.model.AggregateRootEntity
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.DeviceId
import cloud.fabX.fabXaccess.common.model.DeviceIdFactory
import cloud.fabX.fabXaccess.common.model.DomainEvent
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.Error.PinInUse
import cloud.fabX.fabXaccess.common.model.Error.PinNotInUse
import cloud.fabX.fabXaccess.common.model.ToolId
import cloud.fabX.fabXaccess.common.model.assertAggregateVersionIncreasesOneByOne
import cloud.fabX.fabXaccess.common.model.assertAggregateVersionStartsWithOne
import cloud.fabX.fabXaccess.common.model.assertIsNotEmpty
import cloud.fabX.fabXaccess.tool.model.GettingToolById
import cloud.fabX.fabXaccess.user.model.Admin
import kotlinx.datetime.Clock

data class Device internal constructor(
    override val id: DeviceId,
    override val aggregateVersion: Long,
    val name: String,
    val background: String,
    val backupBackendUrl: String,
    val actualFirmwareVersion: String?,
    val desiredFirmwareVersion: String?,
    val attachedTools: Map<Int, ToolId>,
    internal val identity: MacSecretIdentity
) : AggregateRootEntity<DeviceId> {

    companion object {
        fun addNew(
            deviceIdFactory: DeviceIdFactory,
            actor: Admin,
            clock: Clock,
            correlationId: CorrelationId,
            name: String,
            background: String,
            backupBackendUrl: String,
            mac: String,
            secret: String
        ): Either<Error, DeviceSourcingEvent> {
            return MacSecretIdentity.fromUnvalidated(mac, secret, correlationId)
                .map {
                    DeviceCreated(
                        deviceIdFactory.invoke(),
                        actor.id,
                        clock.now(),
                        correlationId,
                        name,
                        background,
                        backupBackendUrl,
                        it.mac,
                        it.secret
                    )
                }
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
        clock: Clock,
        correlationId: CorrelationId,
        name: ChangeableValue<String> = ChangeableValue.LeaveAsIs,
        background: ChangeableValue<String> = ChangeableValue.LeaveAsIs,
        backupBackendUrl: ChangeableValue<String> = ChangeableValue.LeaveAsIs
    ): DeviceSourcingEvent {
        return DeviceDetailsChanged(
            id,
            aggregateVersion + 1,
            actor.id,
            clock.now(),
            correlationId,
            name,
            background,
            backupBackendUrl
        )
    }

    fun setActualFirmwareVersion(
        actor: DeviceActor,
        clock: Clock,
        correlationId: CorrelationId,
        actualFirmwareVersion: String
    ): Either<Error, DeviceSourcingEvent> {
        return this.requireSelfActing(actor, correlationId)
            .map {
                ActualFirmwareVersionChanged(
                    id,
                    aggregateVersion + 1,
                    actor.id,
                    clock.now(),
                    correlationId,
                    actualFirmwareVersion
                )
            }
    }

    fun changeDesiredFirmwareVersion(
        actor: Admin,
        clock: Clock,
        correlationId: CorrelationId,
        desiredFirmwareVersion: String
    ): DeviceSourcingEvent {
        return DesiredFirmwareVersionChanged(
            id,
            aggregateVersion + 1,
            actor.id,
            clock.now(),
            correlationId,
            desiredFirmwareVersion
        )
    }

    private fun requireSelfActing(
        actor: DeviceActor,
        correlationId: CorrelationId
    ): Either<Error, Unit> {
        return if (actor.id == this.id) {
            Unit.right()
        } else {
            Error.DeviceNotActor("Device not actor", correlationId).left()
        }
    }

    suspend fun attachTool(
        actor: Admin,
        clock: Clock,
        correlationId: CorrelationId,
        pin: Int,
        toolId: ToolId,
        gettingToolById: GettingToolById
    ): Either<Error, DeviceSourcingEvent> {
        return attachedTools.getOrNone(pin)
            .toEither {
                ToolAttached(
                    id,
                    aggregateVersion + 1,
                    actor.id,
                    clock.now(),
                    correlationId,
                    pin,
                    toolId
                )
            }
            .map {
                PinInUse("Tool (with id $it) already attached at pin $pin.", pin, correlationId)
            }
            .swap()
            .flatMap { event ->
                // assert tool exists
                gettingToolById.getToolById(toolId)
                    .map { event }
            }
            .mapLeft {
                if (it is Error.ToolNotFound) {
                    Error.ReferencedToolNotFound(
                        it.message,
                        it.toolId,
                        correlationId
                    )
                } else {
                    it
                }
            }
    }

    /**
     * Detaches the tool at the given pin.
     *
     * @return error if the pin is not in use, sourcing event otherwise
     */
    fun detachTool(
        actor: Admin,
        clock: Clock,
        correlationId: CorrelationId,
        pin: Int
    ): Either<Error, DeviceSourcingEvent> =
        detachTool(actor.id, clock, correlationId, pin)

    /**
     * Detaches the tool (triggered by a domain event).
     */
    internal fun detachTool(
        domainEvent: DomainEvent,
        clock: Clock,
        pin: Int
    ): Either<Error, DeviceSourcingEvent> =
        detachTool(domainEvent.actorId, clock, domainEvent.correlationId, pin)

    private fun detachTool(
        actorId: ActorId,
        clock: Clock,
        correlationId: CorrelationId,
        pin: Int
    ): Either<Error, DeviceSourcingEvent> {
        return attachedTools.getOrNone(pin)
            .toEither {
                PinNotInUse("No tool attached at pin $pin.", pin, correlationId)
            }
            .map {
                ToolDetached(
                    id,
                    aggregateVersion + 1,
                    actorId,
                    clock.now(),
                    correlationId,
                    pin
                )
            }
    }

    fun delete(
        actor: Admin,
        clock: Clock,
        correlationId: CorrelationId,
    ): DeviceSourcingEvent {
        return DeviceDeleted(
            id,
            aggregateVersion + 1,
            actor.id,
            clock.now(),
            correlationId
        )
    }

    fun hasIdentity(deviceIdentity: DeviceIdentity) = identity == deviceIdentity

    fun hasAttachedTool(toolId: ToolId): Boolean = attachedTools.any { it.value == toolId }

    fun asActor(): DeviceActor = DeviceActor(id, identity.mac)

    class EventHistoryDoesNotStartWithDeviceCreated(message: String) : Exception(message)
}