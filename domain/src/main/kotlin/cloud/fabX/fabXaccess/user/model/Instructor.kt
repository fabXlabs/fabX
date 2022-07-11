package cloud.fabX.fabXaccess.user.model

import cloud.fabX.fabXaccess.common.model.Actor
import cloud.fabX.fabXaccess.qualification.model.QualificationId

/**
 * An acting instructor.
 */
data class Instructor internal constructor(
    val qualifications: List<QualificationId>
) : Actor