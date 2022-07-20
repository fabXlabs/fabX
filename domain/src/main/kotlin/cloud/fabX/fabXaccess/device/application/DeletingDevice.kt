package cloud.fabX.fabXaccess.device.application

import arrow.core.Option
import arrow.core.flatMap
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.application.logger
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.device.model.DeviceId
import cloud.fabX.fabXaccess.user.model.Admin

/**
 * Service to handle deleting a device.
 */
class DeletingDevice {

    private val log = logger()
    private val deviceRepository = DomainModule.deviceRepository()

    fun deleteDevice(
        actor: Admin,
        correlationId: CorrelationId,
        deviceId: DeviceId
    ): Option<Error> {
        log.debug("deleteDevice...")

        return deviceRepository.getById(deviceId)
            .map {
                it.delete(actor, correlationId)
            }
            .flatMap {
                deviceRepository.store(it)
                    .toEither { }
                    .swap()
            }
            .swap()
            .orNone()
            .tapNone { log.debug("...deleteDevice done") }
            .tap { log.error("...deleteDevice error: $it") }
    }
}