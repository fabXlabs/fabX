package cloud.fabX.fabXaccess.tool.model

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.right
import arrow.core.sequenceEither
import cloud.fabX.fabXaccess.common.model.AggregateRootEntity
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.QualificationId
import cloud.fabX.fabXaccess.common.model.ToolId
import cloud.fabX.fabXaccess.common.model.ToolIdFactory
import cloud.fabX.fabXaccess.common.model.assertAggregateVersionIncreasesOneByOne
import cloud.fabX.fabXaccess.common.model.assertAggregateVersionStartsWithOne
import cloud.fabX.fabXaccess.common.model.assertIsNotEmpty
import cloud.fabX.fabXaccess.qualification.model.GettingQualificationById
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
            toolIdFactory: ToolIdFactory,
            actor: Admin,
            correlationId: CorrelationId,
            name: String,
            type: ToolType,
            time: Int,
            idleState: IdleState,
            wikiLink: String,
            requiredQualifications: Set<QualificationId>,
            gettingQualificationById: GettingQualificationById
        ): Either<Error, ToolSourcingEvent> {
            return requireQualificationsExist(requiredQualifications, gettingQualificationById)
                .map {
                    ToolCreated(
                        toolIdFactory.invoke(),
                        actor.id,
                        correlationId,
                        name,
                        type,
                        time,
                        idleState,
                        wikiLink,
                        requiredQualifications
                    )
                }
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

        private fun requireQualificationsExist(
            qualifications: Set<QualificationId>,
            gettingQualificationById: GettingQualificationById
        ): Either<Error, Unit> {
            return qualifications
                .map(gettingQualificationById::getQualificationById)
                .sequenceEither()
                .map { }
                .mapLeft {
                    if (it is Error.QualificationNotFound) {
                        Error.ReferencedQualificationNotFound(
                            it.message,
                            it.qualificationId
                        )
                    } else {
                        it
                    }
                }
        }
    }

    fun apply(sourcingEvent: ToolSourcingEvent): Option<Tool> =
        sourcingEvent.processBy(ToolEventHandler(), Some(this))

    fun changeDetails(
        actor: Admin,
        correlationId: CorrelationId,
        name: ChangeableValue<String>,
        type: ChangeableValue<ToolType>,
        time: ChangeableValue<Int>,
        idleState: ChangeableValue<IdleState>,
        enabled: ChangeableValue<Boolean>,
        wikiLink: ChangeableValue<String>,
        requiredQualifications: ChangeableValue<Set<QualificationId>>,
        gettingQualificationById: GettingQualificationById
    ): Either<Error, ToolSourcingEvent> {
        return when (requiredQualifications) {
            is ChangeableValue.ChangeToValue -> requireQualificationsExist(
                requiredQualifications.value,
                gettingQualificationById
            )
            is ChangeableValue.LeaveAsIs -> Unit.right()
        }.map {
            ToolDetailsChanged(
                id,
                aggregateVersion + 1,
                actor.id,
                correlationId,
                name,
                type,
                time,
                idleState,
                enabled,
                wikiLink,
                requiredQualifications
            )
        }
    }

    fun delete(
        actor: Admin,
        correlationId: CorrelationId
    ): ToolSourcingEvent {
        return ToolDeleted(
            id,
            aggregateVersion + 1,
            actor.id,
            correlationId
        )
    }

    class EventHistoryDoesNotStartWithToolCreated(message: String) : Exception(message)
}