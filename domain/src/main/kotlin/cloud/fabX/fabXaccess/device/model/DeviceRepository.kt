package cloud.fabX.fabXaccess.device.model

import arrow.core.Either
import arrow.core.Option
import cloud.fabX.fabXaccess.common.model.Error

interface DeviceRepository {
    fun getAll(): Set<Device>
    fun getById(id: DeviceId): Either<Error, Device>
    fun store(event: DeviceSourcingEvent): Option<Error>
}

fun interface GettingDeviceByIdentity {
    fun getByIdentity(identity: DeviceIdentity): Either<Error, Device>
}