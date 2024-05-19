package cloud.fabX.fabXaccess.device.application

import arrow.core.Option
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
    ): Option<Error> {
        log.debug("changeDeviceThumbnail...")

        return deviceRepository.getById(deviceId)
            .flatMap { it.changeThumbnail(actor, correlationId, thumbnail) }
            .flatMap { deviceRepository.storeThumbnail(deviceId, actor.id, it) }
            .swap()
            .getOrNone()
            .onNone { log.debug("...changeDeviceThumbnail done") }
            .onSome { log.error("...changeDeviceThumbnail error: $it") }
    }
}