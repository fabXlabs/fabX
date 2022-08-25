package cloud.fabX.fabXaccess.device.model

import arrow.core.Either
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.DeviceId
import cloud.fabX.fabXaccess.common.model.Error

fun interface RestartDevice {
    suspend fun restartDevice(deviceId: DeviceId, correlationId: CorrelationId): Either<Error, Unit>
}