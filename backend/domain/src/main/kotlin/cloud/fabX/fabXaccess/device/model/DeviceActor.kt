package cloud.fabX.fabXaccess.device.model

import cloud.fabX.fabXaccess.common.model.Actor
import cloud.fabX.fabXaccess.common.model.ActorId
import cloud.fabX.fabXaccess.common.model.DeviceId

/**
 * An acting device. Sometimes acting on behalf of a User.
 */
// TODO acting on behalf of a user (e.g. val onBehalfOf: Member?)
//      - check validity of CardIdentity/PhoneNrIdentity in CommandHandler (or extra service or AuthenticationService?)
//      - use DeviceActor.withActingOnBehalfOf(Member)
data class DeviceActor(
    val deviceId: DeviceId,
    val mac: String
) : Actor {
    override val id: ActorId
        get() = deviceId

    override val name: String
        get() = mac
}