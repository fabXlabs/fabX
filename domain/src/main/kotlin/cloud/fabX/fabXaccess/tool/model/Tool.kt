package cloud.fabX.fabXaccess.tool.model

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.model.AggregateRootEntity
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.assertAggregateVersionIncreasesOneByOne
import cloud.fabX.fabXaccess.common.model.assertAggregateVersionStartsWithOne
import cloud.fabX.fabXaccess.common.model.assertIsNotEmpty
import cloud.fabX.fabXaccess.qualification.model.QualificationId
import cloud.fabX.fabXaccess.user.model.Admin

data class Tool internal constructor(
    override val id: ToolId,
    override val aggregateVersion: Long,
    val name: String,
    val type: ToolType,
    val time: Int, // in ms
    val idleState: IdleState,
    val enabled: Boolean,
    val wikiLink: String,
    val requiredQualifications: Set<QualificationId>
) : AggregateRootEntity<ToolId> {

    companion object {
        fun addNew(
            actor: Admin,
            name: String,
            type: ToolType,
            time: Int,
            idleState: IdleState,
            wikiLink: String,
            requiredQualifications: Set<QualificationId>
        ): ToolSourcingEvent {
            return ToolCreated(
                DomainModule.toolIdFactory().invoke(),
                actor.id,
                name,
                type,
                time,
                idleState,
                wikiLink,
                requiredQualifications
            )
        }

        fun fromSourcingEvents(events: Iterable<ToolSourcingEvent>): Option<Tool> {
            events.assertIsNotEmpty()
            events.assertAggregateVersionStartsWithOne()
            events.assertAggregateVersionIncreasesOneByOne()

            val toolCreatedEvent = events.first()

            if (toolCreatedEvent !is ToolCreated) {
                throw EventHistoryDoesNotStartWithToolCreated(
                    "Event history starts with ${toolCreatedEvent}, not a ToolCreated event."
                )
            }

            return events.fold(None) { result: Option<Tool>, event ->
                event.processBy(ToolEventHandler(), result)
            }
        }
    }

    fun apply(sourcingEvent: ToolSourcingEvent): Option<Tool> =
        sourcingEvent.processBy(ToolEventHandler(), Some(this))

    fun changeDetails(
        actor: Admin,
        name: ChangeableValue<String>,
        type: ChangeableValue<ToolType>,
        time: ChangeableValue<Int>,
        idleState: ChangeableValue<IdleState>,
        enabled: ChangeableValue<Boolean>,
        wikiLink: ChangeableValue<String>,
        requiredQualifications: ChangeableValue<Set<QualificationId>>
    ): ToolSourcingEvent {
        return ToolDetailsChanged(
            id,
            aggregateVersion + 1,
            actor.id,
            name,
            type,
            time,
            idleState,
            enabled,
            wikiLink,
            requiredQualifications
        )
    }

    fun delete(
        actor: Admin
    ): ToolSourcingEvent {
        // TODO remove attachment to/from device(s) (via DomainEvent?)

        return ToolDeleted(
            id,
            aggregateVersion + 1,
            actor.id
        )
    }

    class EventHistoryDoesNotStartWithToolCreated(message: String) : Exception(message)
}