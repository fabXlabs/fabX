package cloud.fabX.fabXaccess.device.infrastructure

import arrow.core.Either
import arrow.core.right
import cloud.fabX.fabXaccess.common.model.DeviceId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.device.model.DevicePinStatus
import cloud.fabX.fabXaccess.device.model.DevicePinStatusRepository

class DevicePinStatusInMemoryRepository : DevicePinStatusRepository {
    private val data = mutableMapOf<DeviceId, DevicePinStatus>()

    override suspend fun getAll(): Set<DevicePinStatus> {
        return data.values.toSet()
    }

    override suspend fun store(devicePinStatus: DevicePinStatus): Either<Error, Unit> {
        data[devicePinStatus.deviceId] = devicePinStatus
        return Unit.right()
    }
}
