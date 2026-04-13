package cloud.fabX.fabXaccess.device.model

import cloud.fabX.fabXaccess.common.model.DeviceId

// TODO extend by creation/update timestamp
data class DevicePinStatus(
    val deviceId: DeviceId,
    val inputPins: Map<Int, Boolean>
)
