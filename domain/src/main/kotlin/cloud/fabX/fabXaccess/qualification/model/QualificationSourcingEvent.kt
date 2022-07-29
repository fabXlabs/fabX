package cloud.fabX.fabXaccess.qualification.model

import arrow.core.Option
import cloud.fabX.fabXaccess.common.model.ActorId
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.QualificationId
import cloud.fabX.fabXaccess.common.model.SourcingEvent
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable


@Serializable
sealed class QualificationSourcingEvent : SourcingEvent {
    abstract override val aggregateRootId: QualificationId
    abstract override val aggregateVersion: Long
    abstract override val actorId: ActorId
    abstract override val correlationId: CorrelationId
    abstract override val timestamp: Instant

    abstract fun processBy(eventHandler: EventHandler, qualification: Option<Qualification>): Option<Qualification>

    interface EventHandler {
        fun handle(event: QualificationCreated, qualification: Option<Qualification>): Option<Qualification>
        fun handle(event: QualificationDetailsChanged, qualification: Option<Qualification>): Option<Qualification>
        fun handle(event: QualificationDeleted, qualification: Option<Qualification>): Option<Qualification>
    }
}

@Serializable
data class QualificationCreated(
    override val aggregateRootId: QualificationId,
    override val actorId: ActorId,
    override val correlationId: CorrelationId,
    val name: String,
    val description: String,
    val colour: String,
    val orderNr: Int,
    override val timestamp: Instant = Clock.System.now()
) : QualificationSourcingEvent() {
    override val aggregateVersion: Long = 1

    override fun processBy(eventHandler: EventHandler, qualification: Option<Qualification>): Option<Qualification> =
        eventHandler.handle(this, qualification)
}

@Serializable
data class QualificationDetailsChanged(
    override val aggregateRootId: QualificationId,
    override val aggregateVersion: Long,
    override val actorId: ActorId,
    override val correlationId: CorrelationId,
    val name: ChangeableValue<String>,
    val description: ChangeableValue<String>,
    val colour: ChangeableValue<String>,
    val orderNr: ChangeableValue<Int>,
    override val timestamp: Instant = Clock.System.now()
) : QualificationSourcingEvent() {

    override fun processBy(eventHandler: EventHandler, qualification: Option<Qualification>): Option<Qualification> =
        eventHandler.handle(this, qualification)
}

@Serializable
data class QualificationDeleted(
    override val aggregateRootId: QualificationId,
    override val aggregateVersion: Long,
    override val actorId: ActorId,
    override val correlationId: CorrelationId,
    override val timestamp: Instant = Clock.System.now(),
) : QualificationSourcingEvent() {

    override fun processBy(eventHandler: EventHandler, qualification: Option<Qualification>): Option<Qualification> =
        eventHandler.handle(this, qualification)
}