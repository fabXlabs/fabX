package cloud.fabX.fabXaccess.user.model

import cloud.fabX.fabXaccess.common.model.Actor
import cloud.fabX.fabXaccess.qualification.model.QualificationId

/**
 * An acting member.
 */
data class Member internal constructor(
    val userId: UserId,
    val qualifications: List<QualificationId>
) : Actor