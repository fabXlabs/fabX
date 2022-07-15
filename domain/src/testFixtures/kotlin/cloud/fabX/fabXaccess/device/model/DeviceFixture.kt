package cloud.fabX.fabXaccess.device.model

import cloud.fabX.fabXaccess.tool.model.ToolId

object DeviceFixture {

    fun arbitrary(
        deviceId: DeviceId = DeviceIdFixture.arbitrary(),
        aggregateVersion: Long = 1,
        name: String = "device",
        background: String = "https://example.com/image.bmp",
        backupBackendUrl: String = "https://fabx-backup.example.com",
        mac: String = "aabbccddeeff",
        secret: String = "supersecret42",
        attachedTools: Map<Int, ToolId> = mapOf()
    ): Device = Device(
        deviceId,
        aggregateVersion,
        name,
        background,
        backupBackendUrl,
        MacSecretIdentity(mac, secret),
        attachedTools
    )

    fun withIdentity(
        deviceIdentity: MacSecretIdentity,
        deviceId: DeviceId = DeviceIdFixture.arbitrary(),
        aggregateVersion: Long = 1,
        name: String = "device",
        background: String = "https://example.com/image.bmp",
        backupBackendUrl: String = "https://fabx-backup.example.com",
        attachedTools: Map<Int, ToolId> = mapOf()
    ) = arbitrary(
        deviceId,
        aggregateVersion,
        name,
        background,
        backupBackendUrl,
        deviceIdentity.mac,
        deviceIdentity.secret,
        attachedTools
    )
}