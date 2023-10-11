package cloud.fabX.fabXaccess.device.application

import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.DomainEvent
import cloud.fabX.fabXaccess.common.model.DomainEventHandler
import cloud.fabX.fabXaccess.common.model.ToolDeleted
import cloud.fabX.fabXaccess.device.model.GettingDevicesByAttachedTool

class DeviceDomainEventHandler(
    loggerFactory: LoggerFactory,
    private val detachingTool: DetachingTool,
    private val gettingDevicesByAttachedTool: GettingDevicesByAttachedTool
) : DomainEventHandler {
    private val log = loggerFactory.invoke(this::class.java)

    override suspend fun handle(domainEvent: DomainEvent) {
        log.debug("ignoring event $domainEvent")
    }

    override suspend fun handle(domainEvent: ToolDeleted) {
        log.debug("handle ToolDeleted event...")
        gettingDevicesByAttachedTool.getByAttachedTool(domainEvent.toolId)
            .flatMap { device ->
                device.attachedTools
                    .filter { it.value == domainEvent.toolId }
                    .map { it.key }
                    .map {
                        detachingTool.detachTool(domainEvent, device.id, it)
                    }
            }
            .mapNotNull { it.getOrNull() }
            .forEach { log.warn("Error while handling domain event ($domainEvent): $it") }
    }
}