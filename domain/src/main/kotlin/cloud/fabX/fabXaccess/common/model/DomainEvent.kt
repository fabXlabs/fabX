package cloud.fabX.fabXaccess.common.model

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
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
    suspend fun handle(domainEvent: CardCreatedAtDevice) = handle(domainEvent as DomainEvent)
}

@OptIn(ExperimentalTime::class)
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

@OptIn(ExperimentalTime::class)
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

@OptIn(ExperimentalTime::class)
data class CardCreatedAtDevice(
    override val actorId: ActorId,
    override val timestamp: Instant = Clock.System.now(),
    override val correlationId: CorrelationId,
    val userId: UserId,
    val cardId: String,
    val cardSecret: String
) : DomainEvent {
    override suspend fun handleBy(eventHandler: DomainEventHandler) {
        eventHandler.handle(this)
    }
}
