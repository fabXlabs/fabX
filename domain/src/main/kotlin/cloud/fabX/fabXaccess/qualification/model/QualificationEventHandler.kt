package cloud.fabX.fabXaccess.qualification.model

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.getOrElse
import cloud.fabX.fabXaccess.common.model.valueToChangeTo

internal class QualificationEventHandler : QualificationSourcingEvent.EventHandler {

    override fun handle(event: QualificationCreated, qualification: Option<Qualification>): Option<Qualification> {
        if (qualification.isSome()) {
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