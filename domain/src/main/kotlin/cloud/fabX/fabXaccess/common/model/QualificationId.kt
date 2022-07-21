package cloud.fabX.fabXaccess.common.model

import java.util.UUID

/**
 * Technical (artificial) ID of a Qualification.
 */
data class QualificationId(override val value: UUID) : EntityId<UUID> {
    companion object {
        fun fromString(s: String): QualificationId {
            return QualificationId(UUID.fromString(s))
        }
    }

    fun serialize(): String {
        return value.toString()
    }
}

typealias QualificationIdFactory = () -> QualificationId

/**
 * Returns a new QualificationId.
 *
 * @return a QualificationId of a random UUID
 */
fun newQualificationId(): QualificationId {
    return QualificationId(UUID.randomUUID())
}