package cloud.fabX.fabXaccess.common.model

import kotlin.uuid.Uuid

object CorrelationIdFixture {

    fun arbitrary(): CorrelationId = newCorrelationId()

    fun static(nr: Int): CorrelationId {
        val byteArray = ByteArray(16)
        byteArray[7] = nr.toByte()
        return CorrelationId(Uuid.fromByteArray(byteArray))
    }
}