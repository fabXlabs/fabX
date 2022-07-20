package cloud.fabX.fabXaccess.device.application

import arrow.core.Either
import arrow.core.Option
import arrow.core.flatMap
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.application.logger
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.DomainEvent
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.device.model.Device
import cloud.fabX.fabXaccess.device.model.DeviceId
import cloud.fabX.fabXaccess.device.model.DeviceSourcingEvent
import cloud.fabX.fabXaccess.user.model.Admin

/**
 * Service for detaching a tool from a device.
 */
class DetachingTool {

    private val log = logger()
    private val deviceRepository = DomainModule.deviceRepository()

    fun detachTool(
        actor: Admin,
        correlationId: CorrelationId,
        deviceId: DeviceId,
        pin: Int
    ): Option<Error> =
        detachTool(deviceId) { it.detachTool(actor, correlationId, pin) }

    internal fun detachTool(
        domainEvent: DomainEvent,
        deviceId: DeviceId,
        pin: Int
    ): Option<Error> =
        detachTool(deviceId) { it.detachTool(domainEvent, pin) }

    private fun detachTool(
        deviceId: DeviceId,
        domainMethod: (Device) -> Either<Error, DeviceSourcingEvent>
    ): Option<Error> {
        log.debug("detachTool...")

        return deviceRepository.getById(deviceId)
            .flatMap { domainMethod(it) }
            .flatMap {
                deviceRepository.store(it)
                    .toEither { }
                    .swap()
            }
            .swap()
            .orNone()
            .tapNone { log.debug("...detachTool done") }
            .tap { log.error("...detachTool error: $it") }
    }
}