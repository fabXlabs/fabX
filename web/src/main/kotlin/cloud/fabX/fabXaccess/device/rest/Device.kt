package cloud.fabX.fabXaccess.device.rest

import cloud.fabX.fabXaccess.common.model.DeviceId
import cloud.fabX.fabXaccess.common.rest.ChangeableValue
import cloud.fabX.fabXaccess.device.model.DevicePinStatus
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Device(
    val id: String,
    val aggregateVersion: Long,
    val name: String,
    val background: String,
    val backupBackendUrl: String,
    val actualFirmwareVersion: String?,
    val desiredFirmwareVersion: String?,
    val attachedTools: Map<Int, String>,
    val attachedInputs: Map<Int, InputDescription>
)

fun cloud.fabX.fabXaccess.device.model.Device.toRestModel(): Device = Device(
    id = id.serialize(),
    aggregateVersion = aggregateVersion,
    name = name,
    background = background,
    backupBackendUrl = backupBackendUrl,
    actualFirmwareVersion = actualFirmwareVersion,
    desiredFirmwareVersion = desiredFirmwareVersion,
    attachedTools = attachedTools.mapValues { it.value.serialize() },
    attachedInputs = attachedInputs.mapValues { it.value.toRestModel() }
)

fun cloud.fabX.fabXaccess.device.model.InputDescription.toRestModel(): InputDescription = InputDescription(
    name = name,
    descriptionLow = descriptionLow,
    descriptionHigh = descriptionHigh,
    colourLow = colourLow,
    colourHigh = colourHigh
)

fun Map<DeviceId, Boolean>.toRestModel(): Map<String, Boolean> {
    return this.mapKeys { it.key.serialize() }
}

fun Set<DevicePinStatus>.toRestModel(): Map<String, PinStatus> {
    return this.associate { it.deviceId.serialize() to it.toRestModel() }
}

fun DevicePinStatus.toRestModel(): PinStatus {
    return PinStatus(
        this.inputPins.mapValues { v ->
            if (v.value) {
                InputPinStatus.INPUT_HIGH
            } else {
                InputPinStatus.INPUT_LOW
            }
        },
        this.updatedAt
    )
}

enum class InputPinStatus {
    INPUT_LOW,
    INPUT_HIGH
}

@Serializable
data class InputDescription(
    val name: String,
    val descriptionLow: String,
    val descriptionHigh: String,
    val colourLow: String,
    val colourHigh: String
)

@Serializable
data class PinStatus(
    // pin number -> input pin status (high/low/unknown)
    val inputPinStatus: Map<Int, InputPinStatus>,
    val updatedAt: Instant
)

@Serializable
data class DeviceCreationDetails(
    val name: String,
    val background: String,
    val backupBackendUrl: String,
    val mac: String,
    val secret: String
)

@Serializable
data class DeviceDetails(
    val name: ChangeableValue<String>?,
    val background: ChangeableValue<String>?,
    val backupBackendUrl: ChangeableValue<String>?
)

@Serializable
data class DesiredFirmwareVersion(
    val desiredFirmwareVersion: String
)

@Serializable
data class ToolAttachmentDetails(
    val toolId: String
)

@Serializable
data class InputAttachmentDetails(
    val name: String,
    val descriptionLow: String,
    val descriptionHigh: String,
    val colourLow: String,
    val colourHigh: String
)

@Serializable
data class ToolUnlockDetails(
    val toolId: String
)

@Serializable
data class AtDeviceCardCreationDetails(
    val userId: String
)