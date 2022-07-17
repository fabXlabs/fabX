package cloud.fabX.fabXaccess.qualification.model

import java.util.UUID

object QualificationIdFixture {

    fun arbitrary(): QualificationId = newQualificationId()

    fun static(nr: Int): QualificationId {
        val byteArray = ByteArray(16)
        byteArray[7] = nr.toByte()
        return QualificationId(UUID.nameUUIDFromBytes(byteArray))
    }

    fun arbitraries(amount: Int): Set<QualificationId> {
        val ids = 1.rangeTo(amount)
            .map { arbitrary() }
            .toSet()

        if (ids.size != amount) {
            throw IllegalStateException("generated duplicate ids")
        }

        return ids
    }
}