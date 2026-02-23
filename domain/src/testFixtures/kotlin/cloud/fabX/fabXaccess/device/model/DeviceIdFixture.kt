package cloud.fabX.fabXaccess.device.model

import cloud.fabX.fabXaccess.common.model.DeviceId
import cloud.fabX.fabXaccess.common.model.newDeviceId
import kotlin.uuid.Uuid

object DeviceIdFixture {

    fun arbitrary():
            DeviceId = newDeviceId()

    fun static(nr: Int): DeviceId {
        val byteArray = ByteArray(16)
        byteArray[7] = nr.toByte()
        return DeviceId(Uuid.fromByteArray(byteArray))
    }
}