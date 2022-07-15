package cloud.fabX.fabXaccess.device.application

import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.model.ErrorFixture
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.device.model.DeviceActor
import cloud.fabX.fabXaccess.device.model.DeviceConfiguration
import cloud.fabX.fabXaccess.device.model.DeviceFixture
import cloud.fabX.fabXaccess.device.model.DeviceIdFixture
import cloud.fabX.fabXaccess.device.model.DeviceRepository
import isLeft
import isRight
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.whenever

@MockitoSettings
internal class GettingConfigurationTest {

    private var logger: Logger? = null
    private var deviceRepository: DeviceRepository? = null

    private var testee: GettingConfiguration? = null

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock deviceRepository: DeviceRepository
    ) {
        this.logger = logger
        this.deviceRepository = deviceRepository

        DomainModule.configureLoggerFactory { logger }
        DomainModule.configureDeviceRepository(deviceRepository)

        testee = GettingConfiguration()
    }

    @Nested
    internal inner class GivenDevice {

        private val deviceId = DeviceIdFixture.arbitrary()

        @BeforeEach
        fun `mock device repository`() {
            val device = DeviceFixture.arbitrary(
                deviceId,
                name = "device42",
                background = "https://example.com/bg.bmp",
                backupBackendUrl = "https://backup.example.com"
            )

            whenever(deviceRepository!!.getById(deviceId))
                .thenReturn(device.right())
        }

        @Test
        fun `when getting configuration then returns configuration`() {
            // given
            val deviceActor = DeviceActor(deviceId, "aabbccddee42")

            // when
            val result = testee!!.getConfiguration(deviceActor)

            // then
            assertThat(result)
                .isRight()
                .isEqualTo(
                    DeviceConfiguration(
                        "device42",
                        "https://example.com/bg.bmp",
                        "https://backup.example.com"
                    )
                )
        }
    }

    @Test
    fun `given device cannot be found when getting configuration then returns error`() {
        // given
        val unknownDeviceId = DeviceIdFixture.arbitrary()
        val unknownDeviceActor = DeviceActor(unknownDeviceId, "aabbccddeeff")

        val expectedError = ErrorFixture.arbitrary()

        whenever(deviceRepository!!.getById(unknownDeviceId))
            .thenReturn(expectedError.left())

        // when
        val result = testee!!.getConfiguration(unknownDeviceActor)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(expectedError)
    }
}