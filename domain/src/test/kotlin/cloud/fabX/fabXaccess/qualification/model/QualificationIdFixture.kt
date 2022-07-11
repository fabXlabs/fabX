package cloud.fabX.fabXaccess.qualification.model

import java.util.UUID

object QualificationIdFixture {

    fun arbitraryId(): QualificationId = newQualificationId()

    fun staticId(nr: Int): QualificationId {
        val byteArray = ByteArray(16)
        byteArray[7] = nr.toByte()
        return QualificationId(UUID.nameUUIDFromBytes(byteArray))
    }

}