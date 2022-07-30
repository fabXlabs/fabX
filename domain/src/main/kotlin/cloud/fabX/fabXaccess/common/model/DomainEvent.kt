package cloud.fabX.fabXaccess.common.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

interface DomainEvent {
    val actorId: ActorId
    val timestamp: Instant
    val correlationId: CorrelationId

    suspend fun handleBy(eventHandler: DomainEventHandler)
}

fun interface DomainEventPublisher {
    suspend fun publish(domainEvent: DomainEvent)
}

interface DomainEventHandler {
    suspend fun handle(domainEvent: DomainEvent)
    suspend fun handle(domainEvent: QualificationDeleted) = handle(domainEvent as DomainEvent)
    suspend fun handle(domainEvent: ToolDeleted) = handle(domainEvent as DomainEvent)
}

data class QualificationDeleted(
    override val actorId: ActorId,
    override val timestamp: Instant = Clock.System.now(),
    override val correlationId: CorrelationId,
    val qualificationId: QualificationId
) : DomainEvent {
    override suspend fun handleBy(eventHandler: DomainEventHandler) {
        eventHandler.handle(this)
    }
}

data class ToolDeleted(
    override val actorId: ActorId,
    override val timestamp: Instant = Clock.System.now(),
    override val correlationId: CorrelationId,
    val toolId: ToolId
) : DomainEvent {
    override suspend fun handleBy(eventHandler: DomainEventHandler) {
        eventHandler.handle(this)
    }
}