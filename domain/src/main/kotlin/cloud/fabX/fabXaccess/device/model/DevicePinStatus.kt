package cloud.fabX.fabXaccess.device.model

import cloud.fabX.fabXaccess.common.model.DeviceId

data class DevicePinStatus(
    val deviceId: DeviceId,
    val inputPins: Map<Int, Boolean>
)
