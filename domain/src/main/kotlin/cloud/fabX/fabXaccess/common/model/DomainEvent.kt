package cloud.fabX.fabXaccess.common.model

import cloud.fabX.fabXaccess.qualification.model.QualificationId
import cloud.fabX.fabXaccess.tool.model.ToolId
import kotlinx.datetime.Instant

// TODO add a trace id
interface DomainEvent {
    val actorId: ActorId
    val timestamp: Instant

    fun handleBy(eventHandler: DomainEventHandler)
}

fun interface DomainEventPublisher {
    fun publish(domainEvent: DomainEvent)
}

interface DomainEventHandler {
    fun handle(domainEvent: DomainEvent)
    fun handle(domainEvent: QualificationDeleted) = handle(domainEvent as DomainEvent)
    fun handle(domainEvent: ToolDeleted) = handle(domainEvent as DomainEvent)
}

data class QualificationDeleted(
    override val actorId: ActorId,
    override val timestamp: Instant,
    val qualificationId: QualificationId
) : DomainEvent {
    override fun handleBy(eventHandler: DomainEventHandler) {
        eventHandler.handle(this)
    }
}

data class ToolDeleted(
    override val actorId: ActorId,
    override val timestamp: Instant,
    val toolId: ToolId
) : DomainEvent {
    override fun handleBy(eventHandler: DomainEventHandler) {
        eventHandler.handle(this)
    }
}