package cloud.fabX.fabXaccess.device.model

import arrow.core.Either
import arrow.core.Option
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.tool.model.ToolId

interface DeviceRepository {
    fun getAll(): Set<Device>
    fun getById(id: DeviceId): Either<Error, Device>
    fun store(event: DeviceSourcingEvent): Option<Error>
}

fun interface GettingDeviceByIdentity {
    fun getByIdentity(identity: DeviceIdentity): Either<Error, Device>
}

fun interface GettingDevicesByAttachedTool {
    fun getByAttachedTool(toolId: ToolId): Set<Device>
}