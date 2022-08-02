package cloud.fabX.fabXaccess.common

import cloud.fabX.fabXaccess.common.model.DomainEvent
import cloud.fabX.fabXaccess.common.model.DomainEventHandler
import cloud.fabX.fabXaccess.common.model.DomainEventPublisher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SynchronousDomainEventPublisher : DomainEventPublisher {

    private val handlers: MutableSet<DomainEventHandler> = mutableSetOf()

    override suspend fun publish(domainEvent: DomainEvent) {
        val jobs = withContext(Dispatchers.Default) {
            handlers.map {
                launch { domainEvent.handleBy(it) }
            }
        }
        jobs.joinAll()
    }

    fun addHandler(handler: DomainEventHandler) {
        handlers.add(handler)
    }

    fun removeHandler(handler: DomainEventHandler) {
        handlers.remove(handler)
    }
}