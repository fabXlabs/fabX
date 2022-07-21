package cloud.fabX.fabXaccess.common.model

import java.util.UUID

/**
 * Technical (artificial) ID of a Device.
 */
data class DeviceId(override val value: UUID) : EntityId<UUID>, ActorId {
    companion object {
        fun fromString(s: String): DeviceId {
            return DeviceId(UUID.fromString(s))
        }
    }

    fun serialize(): String {
        return value.toString()
    }
}

typealias DeviceIdFactory = () -> DeviceId

/**
 * Returns a new DeviceId.
 *
 * @return DeviceId of a random UUID
 */
fun newDeviceId(): DeviceId {
    return DeviceId(UUID.randomUUID())
}