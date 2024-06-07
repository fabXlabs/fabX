package cloud.fabX.fabXaccess.device.application

import arrow.core.Either
import arrow.core.flatMap
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.DeviceId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.device.model.DeviceRepository
import cloud.fabX.fabXaccess.user.model.Admin

/**
 * Service to handle changing device thumbnail.
 */
class ChangingThumbnail(
    loggerFactory: LoggerFactory,
    private val deviceRepository: DeviceRepository
) {
    private val log = loggerFactory.invoke(this::class.java)

    suspend fun changeDeviceThumbnail(
        actor: Admin,
        correlationId: CorrelationId,
        deviceId: DeviceId,
        thumbnail: ByteArray
    ): Either<Error, Unit> =
        log.logError(actor, correlationId, "changeDeviceThumbnail") {
            deviceRepository.getById(deviceId)
                .flatMap { it.changeThumbnail(actor, correlationId, thumbnail) }
                .flatMap { deviceRepository.storeThumbnail(deviceId, actor.id, it) }
        }
}