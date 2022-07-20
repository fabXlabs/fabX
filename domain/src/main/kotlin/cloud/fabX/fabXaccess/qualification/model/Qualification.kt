package cloud.fabX.fabXaccess.qualification.model

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.model.AggregateRootEntity
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.assertAggregateVersionIncreasesOneByOne
import cloud.fabX.fabXaccess.common.model.assertAggregateVersionStartsWithOne
import cloud.fabX.fabXaccess.common.model.assertIsNotEmpty
import cloud.fabX.fabXaccess.tool.model.GettingToolsByQualificationId
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
        fun addNew(
            actor: Admin,
            correlationId: CorrelationId,
            name: String,
            description: String,
            colour: String,
            orderNr: Int
        ): QualificationSourcingEvent {
            return QualificationCreated(
                DomainModule.qualificationIdFactory().invoke(),
                actor.id,
                correlationId,
                name,
                description,
                colour,
                orderNr
            )
        }

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

            return events.fold(None) { result: Option<Qualification>, event ->
                event.processBy(QualificationEventHandler(), result)
            }
        }
    }

    fun apply(sourcingEvent: QualificationSourcingEvent): Option<Qualification> =
        sourcingEvent.processBy(QualificationEventHandler(), Some(this))

    fun changeDetails(
        actor: Admin,
        correlationId: CorrelationId,
        name: ChangeableValue<String> = ChangeableValue.LeaveAsIs,
        description: ChangeableValue<String> = ChangeableValue.LeaveAsIs,
        colour: ChangeableValue<String> = ChangeableValue.LeaveAsIs,
        orderNr: ChangeableValue<Int> = ChangeableValue.LeaveAsIs
    ): QualificationSourcingEvent {
        return QualificationDetailsChanged(
            id,
            aggregateVersion + 1,
            actor.id,
            correlationId,
            name,
            description,
            colour,
            orderNr
        )
    }

    fun delete(
        actor: Admin,
        correlationId: CorrelationId,
        gettingToolsByQualificationId: GettingToolsByQualificationId
    ): Either<Error, QualificationSourcingEvent> {
        return requireQualificationNotInUseByTools(gettingToolsByQualificationId)
            .map {
                QualificationDeleted(
                    id,
                    aggregateVersion + 1,
                    actor.id,
                    correlationId
                )
            }
    }

    private fun requireQualificationNotInUseByTools(
        gettingToolsByQualificationId: GettingToolsByQualificationId
    ): Either<Error, Unit> {
        val tools = gettingToolsByQualificationId.getToolsByQualificationId(id)

        return Either.conditionally(
            tools.isEmpty(),
            {
                val toolIds = tools.joinToString { tool -> tool.id.toString() }

                Error.QualificationInUse(
                    "Qualification in use by tools ($toolIds).",
                    id,
                    tools.map { tool -> tool.id }.toSet()
                )
            },
            {}
        )
    }

    class EventHistoryDoesNotStartWithQualificationCreated(message: String) : Exception(message)
}