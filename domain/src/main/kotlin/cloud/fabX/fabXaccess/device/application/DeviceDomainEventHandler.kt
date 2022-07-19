package cloud.fabX.fabXaccess.device.application

import cloud.fabX.fabXaccess.common.application.logger
import cloud.fabX.fabXaccess.common.model.DomainEvent
import cloud.fabX.fabXaccess.common.model.DomainEventHandler
import cloud.fabX.fabXaccess.common.model.ToolDeleted

class DeviceDomainEventHandler : DomainEventHandler {

    private val log = logger()

    override fun handle(domainEvent: DomainEvent) {
        log.debug("ignoring event $domainEvent")
    }

    override fun handle(domainEvent: ToolDeleted) {
        // TODO remove attachment of tool from device(s)
    }
}