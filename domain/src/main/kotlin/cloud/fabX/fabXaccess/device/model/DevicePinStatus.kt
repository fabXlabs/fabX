package cloud.fabX.fabXaccess.device.model

import cloud.fabX.fabXaccess.common.model.DeviceId
import kotlin.time.Instant

data class DevicePinStatus(
    val deviceId: DeviceId,
    val inputPins: Map<Int, Boolean>,
    val updatedAt: Instant
)
