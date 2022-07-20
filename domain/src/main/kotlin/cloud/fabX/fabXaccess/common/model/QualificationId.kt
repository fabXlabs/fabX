package cloud.fabX.fabXaccess.common.model

import java.util.UUID

/**
 * Technical (artificial) ID of a Qualification.
 */
data class QualificationId(override val value: UUID) : EntityId<UUID>

typealias QualificationIdFactory = () -> QualificationId

/**
 * Returns a new QualificationId.
 *
 * @return a QualificationId of a random UUID
 */
fun newQualificationId(): QualificationId {
    return QualificationId(UUID.randomUUID())
}