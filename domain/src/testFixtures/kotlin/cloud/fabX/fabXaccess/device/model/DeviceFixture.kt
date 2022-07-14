package cloud.fabX.fabXaccess.device.model

object DeviceFixture {

    fun arbitrary(
        deviceId: DeviceId = DeviceIdFixture.arbitraryId(),
        aggregateVersion: Long = 1,
        name: String = "device",
        background: String = "https://example.com/image.bmp",
        backupBackendUrl: String = "https://fabx-backup.example.com"
    ): Device = Device(
        deviceId,
        aggregateVersion,
        name,
        background,
        backupBackendUrl
    )
}