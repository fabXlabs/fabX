package cloud.fabX.fabXaccess.device.application

import arrow.core.Either
import arrow.core.flatMap
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.DeviceId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.device.model.DeviceRepository
import cloud.fabX.fabXaccess.device.model.UpdateDeviceFirmware
import cloud.fabX.fabXaccess.user.model.Admin

class UpdatingDeviceFirmware(
    loggerFactory: LoggerFactory,
    private val deviceRepository: DeviceRepository,
    private val updateDeviceFirmware: UpdateDeviceFirmware
) {
    private val log = loggerFactory.invoke(this::class.java)

    suspend fun updateDeviceFirmware(
        actor: Admin,
        correlationId: CorrelationId,
        deviceId: DeviceId
    ): Either<Error, Unit> {
        return log.logError(actor, correlationId, "updateDeviceFirmware") {
            deviceRepository.getById(deviceId)
                .flatMap { updateDeviceFirmware.updateDeviceFirmware(deviceId, correlationId) }
        }
    }
}