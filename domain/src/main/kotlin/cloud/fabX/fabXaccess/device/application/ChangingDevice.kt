package cloud.fabX.fabXaccess.device.application

import arrow.core.Either
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
    ): Either<Error, Unit> =
        deviceRepository.getAndStoreMap(deviceId, actor, correlationId, log, "changeDeviceDetails") {
            it.changeDetails(actor, clock, correlationId, name, background, backupBackendUrl)
        }
}