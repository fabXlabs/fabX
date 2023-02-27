package cloud.fabX.fabXaccess.device.application

import arrow.core.Either
import arrow.core.flatMap
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
    ): Either<Error, Unit> {
        log.debug("setActualFirmwareVersion...")

        return deviceRepository.getById(deviceId)
            .flatMap {
                it.setActualFirmwareVersion(
                    actor, clock, correlationId, actualFirmwareVersion
                )
            }
            .flatMap {
                deviceRepository.store(it)
                    .toEither { }
                    .swap()
            }
            .onRight { log.debug("...setActualFirmwareVersion done") }
            .onLeft { log.error("...setActualFirmwareVersion error: $it") }
    }

    suspend fun changeDesiredFirmwareVersion(
        actor: Admin,
        correlationId: CorrelationId,
        deviceId: DeviceId,
        desireFirmwareVersion: String
    ): Either<Error, Unit> {
        log.debug("changeDesiredFirmwareVersion...")

        return deviceRepository.getById(deviceId)
            .map {
                it.changeDesiredFirmwareVersion(
                    actor, clock, correlationId, desireFirmwareVersion
                )
            }
            .flatMap {
                deviceRepository.store(it)
                    .toEither { }
                    .swap()
            }
            .onRight { log.debug("...changeDesiredFirmwareVersion done") }
            .onLeft { log.error("...changeDesiredFirmwareVersion error: $it") }
    }
}