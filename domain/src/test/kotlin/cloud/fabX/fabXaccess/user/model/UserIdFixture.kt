package cloud.fabX.fabXaccess.user.model

import java.util.UUID

object UserIdFixture {

    fun arbitraryId(): UserId = newUserId()

    fun staticId(nr: Int): UserId {
        val byteArray = ByteArray(16)
        byteArray[7] = nr.toByte()
        return UserId(UUID.nameUUIDFromBytes(byteArray))
    }

}