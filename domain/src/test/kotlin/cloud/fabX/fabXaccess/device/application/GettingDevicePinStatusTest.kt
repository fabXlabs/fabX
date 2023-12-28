package cloud.fabX.fabXaccess.device.application

import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.model.SystemActor
import cloud.fabX.fabXaccess.device.model.DeviceIdFixture
import cloud.fabX.fabXaccess.device.model.DevicePinStatus
import cloud.fabX.fabXaccess.device.model.DevicePinStatusRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.whenever

@MockitoSettings
internal class GettingDevicePinStatusTest {
    private val correlationId = CorrelationIdFixture.arbitrary()

    private lateinit var logger: Logger
    private lateinit var devicePinStatusRepository: DevicePinStatusRepository

    private lateinit var testee: GettingDevicePinStatus

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock devicePinStatusRepository: DevicePinStatusRepository
    ) {
        this.logger = logger
        this.devicePinStatusRepository = devicePinStatusRepository

        testee = GettingDevicePinStatus({ logger }, devicePinStatusRepository)
    }

    @Test
    fun `when getting device pin status then returns device pin status`() = runTest {
        // given
        val data = setOf(
            DevicePinStatus(
                DeviceIdFixture.arbitrary(),
                mapOf(1 to true, 2 to false)
            ),
            DevicePinStatus(
                DeviceIdFixture.arbitrary(),
                mapOf(1 to false, 2 to true)
            )
        )

        whenever(devicePinStatusRepository.getAll())
            .thenReturn(data)

        // when
        val result = testee.getAll(SystemActor, correlationId)

        // then
        assertThat(result)
            .isEqualTo(data)
    }
}