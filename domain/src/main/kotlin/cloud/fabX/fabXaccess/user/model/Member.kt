package cloud.fabX.fabXaccess.user.model

import cloud.fabX.fabXaccess.common.model.Actor
import cloud.fabX.fabXaccess.common.model.ActorId
import cloud.fabX.fabXaccess.qualification.model.QualificationId

/**
 * An acting member.
 */
data class Member internal constructor(
    val userId: UserId,
    override val name: String,
    val qualifications: Set<QualificationId>
) : Actor {
    override val id: ActorId
        get() = userId
}