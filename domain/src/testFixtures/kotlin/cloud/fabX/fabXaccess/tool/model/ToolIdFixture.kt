package cloud.fabX.fabXaccess.tool.model

import java.util.UUID

object ToolIdFixture {

    fun arbitrary(): ToolId = newToolId()

    fun static(nr: Int): ToolId {
        val byteArray = ByteArray(16)
        byteArray[7] = nr.toByte()
        return ToolId(UUID.nameUUIDFromBytes(byteArray))
    }
}