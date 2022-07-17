package cloud.fabX.fabXaccess.user.model

import cloud.fabX.fabXaccess.common.model.Actor
import cloud.fabX.fabXaccess.common.model.ActorId
import cloud.fabX.fabXaccess.qualification.model.QualificationId

/**
 * An acting instructor.
 */
data class Instructor internal constructor(
    val userId: UserId,
    override val name: String,
    private val qualifications: Set<QualificationId>
) : Actor {
    override val id: ActorId
        get() = userId

    fun hasQualification(qualificationId: QualificationId): Boolean {
        return qualifications.contains(qualificationId)
    }
}