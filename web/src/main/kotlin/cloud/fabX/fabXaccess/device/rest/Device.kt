package cloud.fabX.fabXaccess.device.rest

import cloud.fabX.fabXaccess.common.rest.ChangeableValue
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
    val attachedTools: Map<Int, String>
)

fun cloud.fabX.fabXaccess.device.model.Device.toRestModel(): Device = Device(
    id = id.serialize(),
    aggregateVersion = aggregateVersion,
    name = name,
    background = background,
    backupBackendUrl = backupBackendUrl,
    actualFirmwareVersion = actualFirmwareVersion,
    desiredFirmwareVersion = desiredFirmwareVersion,
    attachedTools = attachedTools.mapValues { it.value.serialize() }
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
data class ToolUnlockDetails(
    val toolId: String
)