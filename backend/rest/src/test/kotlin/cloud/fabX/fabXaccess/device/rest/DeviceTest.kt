package cloud.fabX.fabXaccess.device.rest

import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.device.model.DeviceFixture
import cloud.fabX.fabXaccess.device.model.DeviceIdFixture
import cloud.fabX.fabXaccess.tool.model.ToolIdFixture
import org.junit.jupiter.api.Test

internal class DeviceTest {

    @Test
    fun `when mapping domain model to rest model then returns mapped`() {
        // given
        val toolId1 = ToolIdFixture.arbitrary()
        val toolId2 = ToolIdFixture.arbitrary()
        val toolId3 = ToolIdFixture.arbitrary()

        val deviceId = DeviceIdFixture.arbitrary()
        val device = DeviceFixture.arbitrary(
            deviceId,
            678,
            "device123",
            "https://example.com/background123.bmp",
            "https://backup.example.com",
            "aa11bb22cc33",
            "secret123",
            mapOf(
                1 to toolId1,
                2 to toolId2,
                3 to toolId3
            )
        )

        val expectedResult = Device(
            deviceId.serialize(),
            678,
            "device123",
            "https://example.com/background123.bmp",
            "https://backup.example.com",
            mapOf(
                1 to toolId1.serialize(),
                2 to toolId2.serialize(),
                3 to toolId3.serialize(),
            )
        )

        // when
        val result = device.toRestModel()

        // then
        assertThat(result).isEqualTo(expectedResult)
    }
}