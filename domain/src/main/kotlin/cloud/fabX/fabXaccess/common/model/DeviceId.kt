package cloud.fabX.fabXaccess.common.model

import cloud.fabX.fabXaccess.common.application.UuidSerializer
import java.util.UUID
import kotlinx.serialization.Serializable

/**
 * Technical (artificial) ID of a Device.
 */
@Serializable
data class DeviceId(
    @Serializable(with = UuidSerializer::class) override val value: UUID
) : EntityId<UUID>, ActorId {
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