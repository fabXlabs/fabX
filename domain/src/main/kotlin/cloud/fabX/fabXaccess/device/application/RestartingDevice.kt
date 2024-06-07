package cloud.fabX.fabXaccess.device.application

import arrow.core.Either
import arrow.core.flatMap
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.DeviceId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.device.model.DeviceRepository
import cloud.fabX.fabXaccess.device.model.RestartDevice
import cloud.fabX.fabXaccess.user.model.Admin

class RestartingDevice(
    loggerFactory: LoggerFactory,
    private val deviceRepository: DeviceRepository,
    private val restartDevice: RestartDevice
) {
    private val log = loggerFactory.invoke(this::class.java)

    suspend fun restartDevice(
        actor: Admin,
        correlationId: CorrelationId,
        deviceId: DeviceId
    ): Either<Error, Unit> =
        log.logError(actor, correlationId, "restartDevice") {
            deviceRepository.getById(deviceId)
                .flatMap { restartDevice.restartDevice(deviceId, correlationId) }
        }
}