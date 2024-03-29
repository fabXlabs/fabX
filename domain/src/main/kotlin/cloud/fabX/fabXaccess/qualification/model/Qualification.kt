package cloud.fabX.fabXaccess.qualification.model

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.left
import arrow.core.right
import cloud.fabX.fabXaccess.common.model.AggregateRootEntity
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.QualificationId
import cloud.fabX.fabXaccess.common.model.QualificationIdFactory
import cloud.fabX.fabXaccess.common.model.assertAggregateVersionIncreasesOneByOne
import cloud.fabX.fabXaccess.common.model.assertAggregateVersionStartsWithOne
import cloud.fabX.fabXaccess.common.model.assertIsNotEmpty
import cloud.fabX.fabXaccess.tool.model.GettingToolsByQualificationId
import cloud.fabX.fabXaccess.user.model.Admin
import kotlinx.datetime.Clock

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
            qualificationIdFactory: QualificationIdFactory,
            actor: Admin,
            clock: Clock,
            correlationId: CorrelationId,
            name: String,
            description: String,
            colour: String,
            orderNr: Int,
        ): QualificationSourcingEvent {
            return QualificationCreated(
                qualificationIdFactory.invoke(),
                actor.id,
                clock.now(),
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
        clock: Clock,
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
            clock.now(),
            correlationId,
            name,
            description,
            colour,
            orderNr
        )
    }

    suspend fun delete(
        actor: Admin,
        clock: Clock,
        correlationId: CorrelationId,
        gettingToolsByQualificationId: GettingToolsByQualificationId
    ): Either<Error, QualificationSourcingEvent> {
        return requireQualificationNotInUseByTools(gettingToolsByQualificationId, correlationId)
            .map {
                QualificationDeleted(
                    id,
                    aggregateVersion + 1,
                    actor.id,
                    clock.now(),
                    correlationId
                )
            }
    }

    private suspend fun requireQualificationNotInUseByTools(
        gettingToolsByQualificationId: GettingToolsByQualificationId,
        correlationId: CorrelationId
    ): Either<Error, Unit> {
        val tools = gettingToolsByQualificationId.getToolsByQualificationId(id)

        return if (tools.isNotEmpty()) {
            val toolIds = tools.joinToString { tool -> tool.id.toString() }

            Error.QualificationInUse(
                "Qualification in use by tools ($toolIds).",
                id,
                tools.map { tool -> tool.id }.toSet(),
                correlationId
            ).left()
        } else {
            Unit.right()
        }
    }

    class EventHistoryDoesNotStartWithQualificationCreated(message: String) : Exception(message)
}