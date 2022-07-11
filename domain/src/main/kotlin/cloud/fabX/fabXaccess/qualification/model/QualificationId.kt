package cloud.fabX.fabXaccess.qualification.model

import cloud.fabX.fabXaccess.common.model.EntityId
import java.util.UUID

/**
 * Technical (artificial) ID of a Qualification.
 */
data class QualificationId(override val value: UUID) : EntityId<UUID>

/**
 * Returns a new QualificationId.
 *
 * @return a QualificationId of a random UUID
 */
internal fun newQualificationId(): QualificationId {
    return QualificationId(UUID.randomUUID())
}