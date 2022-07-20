package cloud.fabX.fabXaccess.device.model

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.flatMap
import arrow.core.getOrNone
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.model.ActorId
import cloud.fabX.fabXaccess.common.model.AggregateRootEntity
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.DeviceId
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

data class Device internal constructor(
    override val id: DeviceId,
    override val aggregateVersion: Long,
    val name: String,
    val background: String,
    val backupBackendUrl: String,
    internal val identity: MacSecretIdentity,
    internal val attachedTools: Map<Int, ToolId>
) : AggregateRootEntity<DeviceId> {

    companion object {
        fun addNew(
            actor: Admin,
            correlationId: CorrelationId,
            name: String,
            background: String,
            backupBackendUrl: String,
            identity: MacSecretIdentity
        ): DeviceSourcingEvent {
            return DeviceCreated(
                DomainModule.deviceIdFactory().invoke(),
                actor.id,
                correlationId,
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
        correlationId: CorrelationId,
        name: ChangeableValue<String> = ChangeableValue.LeaveAsIs,
        background: ChangeableValue<String> = ChangeableValue.LeaveAsIs,
        backupBackendUrl: ChangeableValue<String> = ChangeableValue.LeaveAsIs
    ): DeviceSourcingEvent {
        return DeviceDetailsChanged(
            id,
            aggregateVersion + 1,
            actor.id,
            correlationId,
            name,
            background,
            backupBackendUrl
        )
    }

    fun attachTool(
        actor: Admin,
        correlationId: CorrelationId,
        pin: Int,
        toolId: ToolId,
        gettingToolById: GettingToolById
    ): Either<Error, DeviceSourcingEvent> {
        // TODO Check if tool is attached to other device (or other pin on this device)?
        //      Or is it a feature that it can be attached multiple times?

        return attachedTools.getOrNone(pin)
            .toEither {
                ToolAttached(
                    id,
                    aggregateVersion + 1,
                    actor.id,
                    correlationId,
                    pin,
                    toolId
                )
            }
            .map {
                PinInUse("Tool (with id $it) already attached at pin $pin.", pin)
            }
            .swap()
            .flatMap { event ->
                // assert tool exists
                return gettingToolById.getToolById(toolId)
                    .map { event }
            }
    }

    /**
     * Detaches the tool at the given pin.
     *
     * @return error if the pin is not in use, sourcing event otherwise
     */
    fun detachTool(
        actor: Admin,
        correlationId: CorrelationId,
        pin: Int
    ): Either<Error, DeviceSourcingEvent> =
        detachTool(actor.id, correlationId, pin)

    /**
     * Detaches the tool (triggered by a domain event).
     */
    internal fun detachTool(
        domainEvent: DomainEvent,
        pin: Int
    ): Either<Error, DeviceSourcingEvent> =
        detachTool(domainEvent.actorId, domainEvent.correlationId, pin)

    private fun detachTool(
        actorId: ActorId,
        correlationId: CorrelationId,
        pin: Int
    ): Either<Error, DeviceSourcingEvent> {
        return attachedTools.getOrNone(pin)
            .toEither {
                PinNotInUse("No tool attached at pin $pin.", pin)
            }
            .map {
                ToolDetached(
                    id,
                    aggregateVersion + 1,
                    actorId,
                    correlationId,
                    pin
                )
            }
    }

    fun delete(
        actor: Admin,
        correlationId: CorrelationId,
    ): DeviceSourcingEvent {
        return DeviceDeleted(
            id,
            aggregateVersion + 1,
            actor.id,
            correlationId
        )
    }

    fun hasIdentity(deviceIdentity: DeviceIdentity) = identity == deviceIdentity

    fun hasAttachedTool(toolId: ToolId): Boolean = attachedTools.any { it.value == toolId }

    class EventHistoryDoesNotStartWithDeviceCreated(message: String) : Exception(message)
}