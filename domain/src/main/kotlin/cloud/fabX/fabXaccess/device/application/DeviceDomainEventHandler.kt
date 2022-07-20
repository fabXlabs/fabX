package cloud.fabX.fabXaccess.device.application

import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.application.logger
import cloud.fabX.fabXaccess.common.model.DomainEvent
import cloud.fabX.fabXaccess.common.model.DomainEventHandler
import cloud.fabX.fabXaccess.common.model.ToolDeleted
import cloud.fabX.fabXaccess.device.model.GettingDevicesByAttachedTool

class DeviceDomainEventHandler(
    private val detachingTool: DetachingTool = DetachingTool()
) : DomainEventHandler {

    private val log = logger()
    private val gettingDevicesByAttachedTool: GettingDevicesByAttachedTool = DomainModule.gettingDevicesByTool()

    override fun handle(domainEvent: DomainEvent) {
        log.debug("ignoring event $domainEvent")
    }

    override fun handle(domainEvent: ToolDeleted) {
        gettingDevicesByAttachedTool.getByAttachedTool(domainEvent.toolId)
            .flatMap { device ->
                device.attachedTools
                    .filter { it.value == domainEvent.toolId }
                    .map { it.key }
                    .map {
                        detachingTool.detachTool(domainEvent, device.id, it)
                    }
            }
            .mapNotNull { it.orNull() }
            .forEach { log.warn("Error while handling domain event ($domainEvent): $it") }
    }
}