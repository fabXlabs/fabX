package cloud.fabX.fabXaccess.device.application

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.device.model.DeviceActor
import cloud.fabX.fabXaccess.device.model.DevicePinStatus
import cloud.fabX.fabXaccess.device.model.DevicePinStatusRepository

class UpdatingDevicePinStatus(
    loggerFactory: LoggerFactory,
    private val devicePinStatusRepository: DevicePinStatusRepository
) {
    private val log = loggerFactory.invoke(this::class.java)

    suspend fun updateDevicePinStatus(
        actor: DeviceActor,
        correlationId: CorrelationId,
        pinStatus: DevicePinStatus
    ): Either<Error, Unit> =
        log.logError(actor, correlationId, "updateDevicePinStatus") {
            requireActorMatchDevicePinStatus(correlationId, actor, pinStatus)
                .flatMap { devicePinStatusRepository.store(pinStatus) }
        }

    private fun requireActorMatchDevicePinStatus(
        correlationId: CorrelationId,
        actor: DeviceActor,
        pinStatus: DevicePinStatus
    ): Either<Error, Unit> {
        return if (actor.id != pinStatus.deviceId) {
            Error.DeviceNotActor("Device not actor", correlationId).left()
        } else {
            Unit.right()
        }
    }
}