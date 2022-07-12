package cloud.fabX.fabXaccess.qualification.model

import arrow.core.Option
import cloud.fabX.fabXaccess.common.model.ActorId
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.SourcingEvent
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

sealed class QualificationSourcingEvent(
    override val aggregateRootId: QualificationId,
    override val aggregateVersion: Long,
    override val actorId: ActorId,
    override val timestamp: Instant = Clock.System.now()
) : SourcingEvent {

    abstract fun processBy(eventHandler: EventHandler, qualification: Option<Qualification>): Option<Qualification>

    interface EventHandler {
        fun handle(event: QualificationCreated, qualification: Option<Qualification>): Option<Qualification>
        fun handle(event: QualificationDetailsChanged, qualification: Option<Qualification>): Option<Qualification>
    }
}

data class QualificationCreated(
    override val aggregateRootId: QualificationId,
    override val actorId: ActorId,
    val name: String,
    val description: String,
    val colour: String,
    val orderNr: Int
) : QualificationSourcingEvent(aggregateRootId, 1, actorId) {

    override fun processBy(eventHandler: EventHandler, qualification: Option<Qualification>): Option<Qualification> =
        eventHandler.handle(this, qualification)
}

data class QualificationDetailsChanged(
    override val aggregateRootId: QualificationId,
    override val aggregateVersion: Long,
    override val actorId: ActorId,
    val name: ChangeableValue<String>,
    val description: ChangeableValue<String>,
    val colour: ChangeableValue<String>,
    val orderNr: ChangeableValue<Int>
) : QualificationSourcingEvent(aggregateRootId, aggregateVersion, actorId) {
    override fun processBy(eventHandler: EventHandler, qualification: Option<Qualification>): Option<Qualification> =
        eventHandler.handle(this, qualification)
}