package cloud.fabX.fabXaccess.common.model

import kotlinx.datetime.Instant

interface DomainEvent {
    val actorId: ActorId
    val timestamp: Instant
}

fun interface DomainEventPublisher {
    fun publish(domainEvent: DomainEvent)
}

fun interface DomainEventHandler {
    fun handle(domainEvent: DomainEvent)
}