package cloud.fabX.fabXaccess.qualification.model

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

    abstract fun processBy(eventHandler: EventHandler, qualification: Qualification): Qualification

    interface EventHandler {
        fun handle(event: QualificationCreated, qualification: Qualification): Qualification
        fun handle(event: QualificationDetailsChanged, qualification: Qualification): Qualification
    }
}

data class QualificationCreated(
    override val aggregateRootId: QualificationId,
    override val aggregateVersion: Long,
    override val actorId: ActorId,
    val name: String,
    val description: String,
    val colour: String,
    val orderNr: Int
) : QualificationSourcingEvent(aggregateRootId, aggregateVersion, actorId) {

    override fun processBy(eventHandler: EventHandler, qualification: Qualification): Qualification =
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

    override fun processBy(eventHandler: EventHandler, qualification: Qualification): Qualification =
        eventHandler.handle(this, qualification)
}