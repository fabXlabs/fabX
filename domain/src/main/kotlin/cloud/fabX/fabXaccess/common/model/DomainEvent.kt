package cloud.fabX.fabXaccess.common.model

import kotlinx.datetime.Instant

interface DomainEvent {
    val actorId: ActorId
    val timestamp: Instant

    fun handleBy(eventHandler: DomainEventHandler)
}

fun interface DomainEventPublisher {
    fun publish(domainEvent: DomainEvent)
}

interface DomainEventHandler {
    fun handle(domainEvent: QualificationDeleted)
    fun handle(domainEvent: ToolDeleted)
}

data class QualificationDeleted(
    override val actorId: ActorId,
    override val timestamp: Instant
) : DomainEvent {
    override fun handleBy(eventHandler: DomainEventHandler) {
        eventHandler.handle(this)
    }
}

data class ToolDeleted(
    override val actorId: ActorId,
    override val timestamp: Instant
) : DomainEvent {
    override fun handleBy(eventHandler: DomainEventHandler) {
        eventHandler.handle(this)
    }
}