package cloud.fabX.fabXaccess.device.infrastructure

import arrow.core.Either
import arrow.core.left
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

    override suspend fun getById(id: DeviceId): Either<Error, DevicePinStatus> {
        return data[id]?.let { return it.right() } ?:
            Error.DevicePinStatusUnknown(
                "Pin status of Device with id $id not known.",
                id
            ).left()
    }

    override suspend fun store(devicePinStatus: DevicePinStatus): Either<Error, Unit> {
        data[devicePinStatus.deviceId] = devicePinStatus
        return Unit.right()
    }
}
