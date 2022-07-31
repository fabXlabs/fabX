package cloud.fabX.fabXaccess.user.model

import cloud.fabX.fabXaccess.common.model.Actor
import cloud.fabX.fabXaccess.common.model.ActorId
import cloud.fabX.fabXaccess.common.model.UserId

/**
 * An acting administrator.
 */
data class Admin(
    val userId: UserId,
    override val name: String
) : Actor {
    override val id: ActorId
        get() = userId
}