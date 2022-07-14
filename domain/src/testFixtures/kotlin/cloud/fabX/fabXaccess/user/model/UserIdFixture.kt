package cloud.fabX.fabXaccess.user.model

import java.util.UUID

object UserIdFixture {

    fun arbitrary(): UserId = newUserId()

    fun static(nr: Int): UserId {
        val byteArray = ByteArray(16)
        byteArray[7] = nr.toByte()
        return UserId(UUID.nameUUIDFromBytes(byteArray))
    }
}