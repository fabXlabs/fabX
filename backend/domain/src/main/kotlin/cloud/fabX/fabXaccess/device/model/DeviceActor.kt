package cloud.fabX.fabXaccess.device.model

import cloud.fabX.fabXaccess.common.model.Actor
import cloud.fabX.fabXaccess.common.model.ActorId
import cloud.fabX.fabXaccess.common.model.DeviceId
import cloud.fabX.fabXaccess.user.model.Member

/**
 * An acting device. May be acting on behalf of a User.
 */
data class DeviceActor(
    val deviceId: DeviceId,
    val mac: String,
    val onBehalfOf: Member? = null
) : Actor {
    override val id: ActorId
        get() = deviceId

    override val name: String
        get() = mac
}