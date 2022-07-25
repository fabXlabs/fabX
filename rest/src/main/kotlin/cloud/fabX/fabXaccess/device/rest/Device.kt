package cloud.fabX.fabXaccess.device.rest

import kotlinx.serialization.Serializable

@Serializable
data class Device(
    val id: String,
    val aggregateVersion: Long,
    val name: String,
    val background: String,
    val backupBackendUrl: String,
    val attachedTools: Map<Int, String>
)

fun cloud.fabX.fabXaccess.device.model.Device.toRestModel(): Device = Device(
    id = id.serialize(),
    aggregateVersion = aggregateVersion,
    name = name,
    background = background,
    backupBackendUrl = backupBackendUrl,
    attachedTools = attachedTools.mapValues { it.value.serialize() }
)