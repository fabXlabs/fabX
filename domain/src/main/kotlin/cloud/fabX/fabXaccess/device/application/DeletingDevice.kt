package cloud.fabX.fabXaccess.device.application

import arrow.core.Option
import arrow.core.flatMap
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.DeviceId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.device.model.DeviceRepository
import cloud.fabX.fabXaccess.user.model.Admin
import kotlinx.datetime.Clock

/**
 * Service to handle deleting a device.
 */
class DeletingDevice(
    loggerFactory: LoggerFactory,
    private val deviceRepository: DeviceRepository,
    private val clock: Clock
) {
    private val log = loggerFactory.invoke(this::class.java)

    fun deleteDevice(
        actor: Admin,
        correlationId: CorrelationId,
        deviceId: DeviceId
    ): Option<Error> {
        log.debug("deleteDevice...")

        return deviceRepository.getById(deviceId)
            .map {
                it.delete(actor, clock, correlationId)
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