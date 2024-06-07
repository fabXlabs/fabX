package cloud.fabX.fabXaccess.device.application

import arrow.core.Either
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.DeviceId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.device.model.DeviceActor
import cloud.fabX.fabXaccess.device.model.DeviceRepository
import cloud.fabX.fabXaccess.user.model.Admin
import kotlinx.datetime.Clock

/**
 * Service to handle actual and desired device firmware version.
 */
class ChangingFirmwareVersion(
    loggerFactory: LoggerFactory,
    private val deviceRepository: DeviceRepository,
    private val clock: Clock
) {
    private val log = loggerFactory.invoke(this::class.java)

    suspend fun setActualFirmwareVersion(
        actor: DeviceActor,
        correlationId: CorrelationId,
        deviceId: DeviceId,
        actualFirmwareVersion: String
    ): Either<Error, Unit> =
        deviceRepository.getAndStoreFlatMap(deviceId, actor, correlationId, log, "setActualFirmwareVersion") {
            it.setActualFirmwareVersion(
                actor, clock, correlationId, actualFirmwareVersion
            )
        }

    suspend fun changeDesiredFirmwareVersion(
        actor: Admin,
        correlationId: CorrelationId,
        deviceId: DeviceId,
        desireFirmwareVersion: String
    ): Either<Error, Unit> =
        deviceRepository.getAndStoreMap(deviceId, actor, correlationId, log, "changeDesiredFirmwareVersion") {
            it.changeDesiredFirmwareVersion(
                actor, clock, correlationId, desireFirmwareVersion
            )
        }
}