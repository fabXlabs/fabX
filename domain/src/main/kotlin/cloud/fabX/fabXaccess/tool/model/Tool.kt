package cloud.fabX.fabXaccess.tool.model

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.getOrElse
import arrow.core.raise.either
import arrow.core.right
import cloud.fabX.fabXaccess.common.application.ThumbnailCreator
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
import cloud.fabX.fabXaccess.common.model.toReferencedQualificationNotFound
import cloud.fabX.fabXaccess.qualification.model.GettingQualificationById
import cloud.fabX.fabXaccess.user.model.Admin
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
data class Tool internal constructor(
    override val id: ToolId,
    override val aggregateVersion: Long,
    val name: String,
    val type: ToolType,
    val requires2FA: Boolean, // second factor authentication
    val time: Int, // in ms
    val idleState: IdleState,
    val enabled: Boolean,
    val notes: String?,
    val wikiLink: String,
    val requiredQualifications: Set<QualificationId>
) : AggregateRootEntity<ToolId> {

    companion object {
        suspend fun addNew(
            toolIdFactory: ToolIdFactory,
            actor: Admin,
            clock: Clock,
            correlationId: CorrelationId,
            name: String,
            type: ToolType,
            requires2FA: Boolean,
            time: Int,
            idleState: IdleState,
            wikiLink: String,
            requiredQualifications: Set<QualificationId>,
            gettingQualificationById: GettingQualificationById
        ): Either<Error, ToolSourcingEvent> {
            return requireQualificationsExist(requiredQualifications, gettingQualificationById, correlationId)
                .map {
                    ToolCreated(
                        toolIdFactory.invoke(),
                        actor.id,
                        clock.now(),
                        correlationId,
                        name,
                        type,
                        requires2FA,
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

        private suspend fun requireQualificationsExist(
            qualifications: Set<QualificationId>,
            gettingQualificationById: GettingQualificationById,
            correlationId: CorrelationId
        ): Either<Error, Unit> {
            return qualifications
                .map { gettingQualificationById.getQualificationById(it) }
                .let { eithers -> either { eithers.bindAll() } }
                .map { }
                .mapLeft {
                    if (it is Error.QualificationNotFound) {
                        it.toReferencedQualificationNotFound(correlationId)
                    } else {
                        it
                    }
                }
        }
    }

    fun apply(sourcingEvent: ToolSourcingEvent): Option<Tool> =
        sourcingEvent.processBy(ToolEventHandler(), Some(this))

    suspend fun changeDetails(
        actor: Admin,
        clock: Clock,
        correlationId: CorrelationId,
        name: ChangeableValue<String>,
        type: ChangeableValue<ToolType>,
        requires2FA: ChangeableValue<Boolean>,
        time: ChangeableValue<Int>,
        idleState: ChangeableValue<IdleState>,
        enabled: ChangeableValue<Boolean>,
        notes: ChangeableValue<String?>,
        wikiLink: ChangeableValue<String>,
        requiredQualifications: ChangeableValue<Set<QualificationId>>,
        gettingQualificationById: GettingQualificationById
    ): Either<Error, ToolSourcingEvent> {
        return when (requiredQualifications) {
            is ChangeableValue.ChangeToValue -> requireQualificationsExist(
                requiredQualifications.value,
                gettingQualificationById,
                correlationId
            )

            is ChangeableValue.LeaveAsIs -> Unit.right()
        }.map {
            ToolDetailsChanged(
                id,
                aggregateVersion + 1,
                actor.id,
                clock.now(),
                correlationId,
                name,
                type,
                requires2FA,
                time,
                idleState,
                enabled,
                notes,
                wikiLink,
                requiredQualifications
            )
        }
    }

    fun changeThumbnail(
        actor: Admin,
        correlationId: CorrelationId,
        image: ByteArray
    ): Either<Error, ByteArray> =
        ThumbnailCreator.create(image, correlationId)

    fun getThumbnail(
        actor: Admin,
        correlationId: CorrelationId,
        imageFromRepository: Either<Error, ByteArray>
    ): ByteArray =
        imageFromRepository.getOrElse { ThumbnailCreator.default }

    fun delete(
        actor: Admin,
        clock: Clock,
        correlationId: CorrelationId
    ): ToolSourcingEvent {
        return ToolDeleted(
            id,
            aggregateVersion + 1,
            actor.id,
            clock.now(),
            correlationId
        )
    }

    class EventHistoryDoesNotStartWithToolCreated(message: String) : Exception(message)
}