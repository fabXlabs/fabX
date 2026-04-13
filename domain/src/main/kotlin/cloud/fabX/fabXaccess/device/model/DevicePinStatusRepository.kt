package cloud.fabX.fabXaccess.device.model

import arrow.core.Either
import cloud.fabX.fabXaccess.common.model.DeviceId
import cloud.fabX.fabXaccess.common.model.Error

interface DevicePinStatusRepository {
    suspend fun getAll(): Set<DevicePinStatus>
    suspend fun getById(id: DeviceId): Either<Error, DevicePinStatus>
    suspend fun store(devicePinStatus: DevicePinStatus): Either<Error, Unit>
}
