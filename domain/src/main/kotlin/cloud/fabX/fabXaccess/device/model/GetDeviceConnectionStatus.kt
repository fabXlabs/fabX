package cloud.fabX.fabXaccess.device.model

import cloud.fabX.fabXaccess.common.model.DeviceId

interface GetDeviceConnectionStatus {
    fun isConnected(deviceId: DeviceId): Boolean
}
