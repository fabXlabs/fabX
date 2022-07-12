package cloud.fabX.fabXaccess.qualification.model

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

data class Qualification internal constructor(
    override val id: QualificationId,
    override val aggregateVersion: Long,
    val name: String,
    val description: String,
    val colour: String,
    val orderNr: Int
) : AggregateRootEntity<QualificationId> {

    companion object {
        fun fromSourcingEvents(events: Iterable<QualificationSourcingEvent>): Option<Qualification> {
            events.assertIsNotEmpty()
            events.assertAggregateVersionStartsWithOne()
            events.assertAggregateVersionIncreasesOneByOne()

            val qualificationCreatedEvent = events.first()

            if (qualificationCreatedEvent !is QualificationCreated) {
                throw EventHistoryDoesNotStartWithQualificationCreated(
                    "Event history starts with ${qualificationCreatedEvent}, not a QualificationCreated event."
                )
            }

            return events.fold(
                None
            ) { result: Option<Qualification>, event ->
                event.processBy(EventHandler(), result)
            }
        }
    }

    fun apply(sourcingEvent: QualificationSourcingEvent): Option<Qualification> =
        sourcingEvent.processBy(EventHandler(), Some(this))

    fun changeDetails(
        actor: Admin,
        name: ChangeableValue<String> = ChangeableValue.LeaveAsIs,
        description: ChangeableValue<String> = ChangeableValue.LeaveAsIs,
        colour: ChangeableValue<String> = ChangeableValue.LeaveAsIs,
        orderNr: ChangeableValue<Int> = ChangeableValue.LeaveAsIs
    ): QualificationSourcingEvent {
        return QualificationDetailsChanged(
            id,
            aggregateVersion + 1,
            actor.id,
            name,
            description,
            colour,
            orderNr
        )
    }

    fun delete(
        actor: Admin
    ): QualificationSourcingEvent {
        return QualificationDeleted(
            id,
            aggregateVersion + 1,
            actor.id
        )
    }

    private class EventHandler : QualificationSourcingEvent.EventHandler {

        override fun handle(event: QualificationCreated, qualification: Option<Qualification>): Option<Qualification> {
            if (qualification.isDefined()) {
                throw AccumulatorNotEmptyForQualificationCreatedEventHandler(
                    "Handler for QualificationCreated is given $qualification."
                )
            }

            return Some(
                Qualification(
                    id = event.aggregateRootId,
                    aggregateVersion = event.aggregateVersion,
                    name = event.name,
                    description = event.description,
                    colour = event.colour,
                    orderNr = event.orderNr
                )
            )
        }

        override fun handle(
            event: QualificationDetailsChanged,
            qualification: Option<Qualification>
        ): Option<Qualification> = requireSomeQualificationWithSameIdAnd(event, qualification) { e, q ->
            Some(
                q.copy(
                    aggregateVersion = e.aggregateVersion,
                    name = e.name.valueToChangeTo(q.name),
                    description = e.description.valueToChangeTo(q.description),
                    colour = e.colour.valueToChangeTo(q.colour),
                    orderNr = e.orderNr.valueToChangeTo(q.orderNr)
                )
            )
        }

        override fun handle(
            event: QualificationDeleted,
            qualification: Option<Qualification>
        ): Option<Qualification> = requireSomeQualificationWithSameIdAnd(event, qualification) { _, _ ->
            None
        }

        private fun <E : QualificationSourcingEvent> requireSomeQualificationWithSameIdAnd(
            event: E,
            qualification: Option<Qualification>,
            and: (E, Qualification) -> Option<Qualification>
        ): Option<Qualification> {
            if (qualification.map { it.id != event.aggregateRootId }.getOrElse { false }) {
                throw EventAggregateRootIdDoesNotMatchQualificationId(
                    "Event $event cannot be applied to $qualification. Aggregate root id does not match."
                )
            }

            return qualification.flatMap { and(event, it) }
        }

        class EventAggregateRootIdDoesNotMatchQualificationId(message: String) : Exception(message)
        class AccumulatorNotEmptyForQualificationCreatedEventHandler(message: String) : Exception(message)
    }

    class EventHistoryDoesNotStartWithQualificationCreated(message: String) : Exception(message)
}