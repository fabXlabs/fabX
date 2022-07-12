package cloud.fabX.fabXaccess.device.model

import cloud.fabX.fabXaccess.common.model.Actor
import cloud.fabX.fabXaccess.common.model.ActorId

/**
 * An acting device. Sometimes acting on behalf of a User.
 */
data class DeviceActor(
    val deviceId: DeviceId,
    val mac: String
) : Actor {
    override val id: ActorId
        get() = deviceId

    override val name: String
        get() = mac
}