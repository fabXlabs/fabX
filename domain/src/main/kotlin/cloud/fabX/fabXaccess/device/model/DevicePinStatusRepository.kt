package cloud.fabX.fabXaccess.device.model

import arrow.core.Either

interface DevicePinStatusRepository {
    suspend fun getAll(): Set<DevicePinStatus>
    suspend fun store(devicePinStatus: DevicePinStatus): Either<Error, Unit>
}
