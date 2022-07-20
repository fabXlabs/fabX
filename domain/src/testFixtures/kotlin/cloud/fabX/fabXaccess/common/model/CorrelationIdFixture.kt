package cloud.fabX.fabXaccess.common.model

import java.util.UUID

object CorrelationIdFixture {

    fun arbitrary(): CorrelationId = newCorrelationId()

    fun static(nr: Int): CorrelationId {
        val byteArray = ByteArray(16)
        byteArray[7] = nr.toByte()
        return CorrelationId(UUID.nameUUIDFromBytes(byteArray))
    }
}