package cloud.fabX.fabXaccess.qualification.model

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
        fun fromSourcingEvents(events: Iterable<QualificationSourcingEvent>): Qualification {
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
                Qualification(
                    qualificationCreatedEvent.aggregateRootId,
                    qualificationCreatedEvent.aggregateVersion,
                    qualificationCreatedEvent.name,
                    qualificationCreatedEvent.description,
                    qualificationCreatedEvent.colour,
                    qualificationCreatedEvent.orderNr
                )
            ) { qualification, event ->
                qualification.apply(event)
            }
        }
    }

    fun apply(sourcingEvent: QualificationSourcingEvent): Qualification = sourcingEvent.processBy(EventHandler(), this)

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

    private class EventHandler : QualificationSourcingEvent.EventHandler {
        override fun handle(event: QualificationCreated, qualification: Qualification): Qualification = Qualification(
            id = event.aggregateRootId,
            aggregateVersion = event.aggregateVersion,
            name = event.name,
            description = event.description,
            colour = event.colour,
            orderNr = event.orderNr
        )

        override fun handle(event: QualificationDetailsChanged, qualification: Qualification): Qualification =
            requireSameQualificationIdAnd(event, qualification) { e, q ->
                q.copy(
                    aggregateVersion = e.aggregateVersion,
                    name = e.name.valueToChangeTo(q.name),
                    description = e.description.valueToChangeTo(q.description),
                    colour = e.colour.valueToChangeTo(q.colour),
                    orderNr = e.orderNr.valueToChangeTo(q.orderNr)
                )
            }

        private fun <E : QualificationSourcingEvent> requireSameQualificationIdAnd(
            event: E,
            qualification: Qualification,
            and: (E, Qualification) -> Qualification
        ): Qualification {
            if (event.aggregateRootId != qualification.id) {
                throw EventAggregateRootIdDoesNotMatchQualificationId("Event $event cannot be applied to $qualification.")
            }
            return and(event, qualification)
        }

        class EventAggregateRootIdDoesNotMatchQualificationId(message: String) : Exception(message)
    }

    class EventHistoryDoesNotStartWithQualificationCreated(message: String) : Exception(message)
}