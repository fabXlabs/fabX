package cloud.fabX.fabXaccess.tool.model

import cloud.fabX.fabXaccess.common.model.ToolId
import cloud.fabX.fabXaccess.common.model.newToolId
import java.util.UUID

object ToolIdFixture {

    fun arbitrary(): ToolId = newToolId()

    fun static(nr: Int): ToolId {
        val byteArray = ByteArray(16)
        byteArray[7] = nr.toByte()
        return ToolId(UUID.nameUUIDFromBytes(byteArray))
    }
}