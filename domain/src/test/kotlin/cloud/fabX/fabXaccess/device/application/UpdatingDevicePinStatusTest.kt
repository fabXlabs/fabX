package cloud.fabX.fabXaccess.device.application

import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.device.model.DeviceFixture
import cloud.fabX.fabXaccess.device.model.DeviceIdFixture
import cloud.fabX.fabXaccess.device.model.DevicePinStatus
import cloud.fabX.fabXaccess.device.model.DevicePinStatusRepository
import isLeft
import isRight
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.whenever

@MockitoSettings
internal class UpdatingDevicePinStatusTest {
    private val deviceId = DeviceIdFixture.arbitrary()
    private val correlationId = CorrelationIdFixture.arbitrary()

    private lateinit var logger: Logger
    private lateinit var devicePinStatusRepository: DevicePinStatusRepository

    private lateinit var testee: UpdatingDevicePinStatus

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock devicePinStatusRepository: DevicePinStatusRepository
    ) {
        this.logger = logger
        this.devicePinStatusRepository = devicePinStatusRepository

        testee = UpdatingDevicePinStatus({ logger }, devicePinStatusRepository)
    }

    @Test
    fun `when updating device pin status then stores new status`() = runTest {
        // given
        val device = DeviceFixture.arbitrary(deviceId)

        val pinStatus = DevicePinStatus(
            deviceId,
            mapOf(
                1 to true,
                2 to false,
                3 to false,
                4 to true
            )
        )

        whenever(devicePinStatusRepository.store(pinStatus))
            .thenReturn(Unit.right())

        // when
        val result = testee.updateDevicePinStatus(
            device.asActor(),
            correlationId,
            pinStatus
        )

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(Unit)
    }

    @Test
    fun `given device id not id in pinStatus when updating device pin status then returns error`() = runTest {
        // given
        val device = DeviceFixture.arbitrary(deviceId)

        val pinStatus = DevicePinStatus(
            DeviceIdFixture.arbitrary(), // device id not of actor
            mapOf(
                1 to true,
                2 to false,
                3 to false,
                4 to true
            )
        )

        // when
        val result = testee.updateDevicePinStatus(
            device.asActor(),
            correlationId,
            pinStatus
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.DeviceNotActor("Device not actor", correlationId)
            )
    }
}