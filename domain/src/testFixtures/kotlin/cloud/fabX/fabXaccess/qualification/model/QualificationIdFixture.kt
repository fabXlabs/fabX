package cloud.fabX.fabXaccess.qualification.model

import java.util.UUID

object QualificationIdFixture {

    fun arbitrary(): QualificationId = newQualificationId()

    fun static(nr: Int): QualificationId {
        val byteArray = ByteArray(16)
        byteArray[7] = nr.toByte()
        return QualificationId(UUID.nameUUIDFromBytes(byteArray))
    }
}