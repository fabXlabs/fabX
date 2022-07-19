package cloud.fabX.fabXaccess.common

import cloud.fabX.fabXaccess.common.model.DomainEvent
import cloud.fabX.fabXaccess.common.model.DomainEventHandler
import cloud.fabX.fabXaccess.common.model.DomainEventPublisher

class SynchronousDomainEventPublisher : DomainEventPublisher {

    private val handlers: MutableSet<DomainEventHandler> = mutableSetOf()

    override fun publish(domainEvent: DomainEvent) {
        handlers.forEach { it.handle(domainEvent) }
    }

    fun addHandler(handler: DomainEventHandler) {
        handlers.add(handler)
    }

    fun removeHandler(handler: DomainEventHandler) {
        handlers.remove(handler)
    }
}