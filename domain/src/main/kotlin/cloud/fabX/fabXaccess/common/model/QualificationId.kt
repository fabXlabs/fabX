package cloud.fabX.fabXaccess.common.model

import cloud.fabX.fabXaccess.common.application.UuidSerializer
import java.util.UUID
import kotlinx.serialization.Serializable

/**
 * Technical (artificial) ID of a Qualification.
 */
@Serializable
data class QualificationId(
    @Serializable(with = UuidSerializer::class) override val value: UUID
) : EntityId<UUID> {
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