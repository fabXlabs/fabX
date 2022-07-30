package cloud.fabX.fabXaccess.common

import cloud.fabX.fabXaccess.common.model.DomainEvent
import cloud.fabX.fabXaccess.common.model.DomainEventHandler
import cloud.fabX.fabXaccess.common.model.DomainEventPublisher

class SynchronousDomainEventPublisher : DomainEventPublisher {

    private val handlers: MutableSet<DomainEventHandler> = mutableSetOf()

    override suspend fun publish(domainEvent: DomainEvent) {
        handlers.forEach { domainEvent.handleBy(it) }
    }

    fun addHandler(handler: DomainEventHandler) {
        handlers.add(handler)
    }

    fun removeHandler(handler: DomainEventHandler) {
        handlers.remove(handler)
    }
}