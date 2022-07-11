package cloud.fabX.fabXaccess.device.model

import cloud.fabX.fabXaccess.common.model.ActorId
import cloud.fabX.fabXaccess.common.model.EntityId
import java.util.UUID

/**
 * Technical (artificial) ID of a Device.
 */
data class DeviceId(override val value: UUID) : EntityId<UUID>, ActorId

/**
 * Returns a new DeviceId.
 *
 * @returna DeviceId of a random UUID
 */
internal fun newDeviceId(): DeviceId {
    return DeviceId(UUID.randomUUID())
}