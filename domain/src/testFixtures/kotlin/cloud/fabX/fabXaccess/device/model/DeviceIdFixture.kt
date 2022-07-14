package cloud.fabX.fabXaccess.device.model

import java.util.UUID

object DeviceIdFixture {

    fun arbitrary(): DeviceId = newDeviceId()

    fun static(nr: Int): DeviceId {
        val byteArray = ByteArray(16)
        byteArray[7] = nr.toByte()
        return DeviceId(UUID.nameUUIDFromBytes(byteArray))
    }
}