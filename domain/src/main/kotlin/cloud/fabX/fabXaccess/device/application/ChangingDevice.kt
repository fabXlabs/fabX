package cloud.fabX.fabXaccess.device.application

import arrow.core.Option
import arrow.core.flatMap
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.DeviceId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.device.model.DeviceRepository
import cloud.fabX.fabXaccess.user.model.Admin
import kotlinx.datetime.Clock

/**
 * Service to handle changing device properties.
 */
class ChangingDevice(
    loggerFactory: LoggerFactory,
    private val deviceRepository: DeviceRepository,
    private val clock: Clock
) {
    private val log = loggerFactory.invoke(this::class.java)

    suspend fun changeDeviceDetails(
        actor: Admin,
        correlationId: CorrelationId,
        deviceId: DeviceId,
        name: ChangeableValue<String>,
        background: ChangeableValue<String>,
        backupBackendUrl: ChangeableValue<String>
    ): Option<Error> {
        log.debug("changeDeviceDetails...")

        return deviceRepository.getById(deviceId)
            .map {
                it.changeDetails(actor, clock, correlationId, name, background, backupBackendUrl)
            }
            .flatMap {
                deviceRepository.store(it)
                    .toEither { }
                    .swap()
            }
            .swap()
            .getOrNone()
            .onNone { log.debug("...changeDeviceDetails done") }
            .onSome { log.error("...changeDeviceDetails error: $it") }
    }
}