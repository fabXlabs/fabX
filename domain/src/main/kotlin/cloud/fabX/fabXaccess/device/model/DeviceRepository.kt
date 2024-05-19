package cloud.fabX.fabXaccess.device.model

import arrow.core.Either
import arrow.core.Option
import cloud.fabX.fabXaccess.common.model.ActorId
import cloud.fabX.fabXaccess.common.model.DeviceId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.ToolId

interface DeviceRepository {
    suspend fun getAll(): Set<Device>
    suspend fun getById(id: DeviceId): Either<Error, Device>
    suspend fun store(event: DeviceSourcingEvent): Option<Error>

    suspend fun storeThumbnail(id: DeviceId, actor: ActorId, thumbnail: ByteArray): Either<Error, Unit>
    suspend fun getThumbnail(id: DeviceId): Either<Error, ByteArray>
}

fun interface GettingDeviceByIdentity {
    suspend fun getByIdentity(identity: DeviceIdentity): Either<Error, Device>
}

fun interface GettingDevicesByAttachedTool {
    suspend fun getByAttachedTool(toolId: ToolId): Set<Device>
}