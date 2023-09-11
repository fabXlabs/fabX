package cloud.fabX.fabXaccess.device.application

import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.ErrorFixture
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.device.model.DeviceFixture
import cloud.fabX.fabXaccess.device.model.DeviceIdFixture
import cloud.fabX.fabXaccess.device.model.DeviceRepository
import cloud.fabX.fabXaccess.device.model.RestartDevice
import cloud.fabX.fabXaccess.user.model.AdminFixture
import isLeft
import isRight
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.whenever

@MockitoSettings
internal class RestartingDeviceTest {

    private val adminActor = AdminFixture.arbitrary()
    private val deviceId = DeviceIdFixture.arbitrary()
    private val correlationId = CorrelationIdFixture.arbitrary()

    private lateinit var logger: Logger
    private lateinit var deviceRepository: DeviceRepository
    private lateinit var restartDevice: RestartDevice

    private lateinit var testee: RestartingDevice

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock deviceRepository: DeviceRepository,
        @Mock restartDevice: RestartDevice
    ) {
        this.logger = logger
        this.deviceRepository = deviceRepository
        this.restartDevice = restartDevice

        testee = RestartingDevice({ logger }, deviceRepository, restartDevice)
    }

    @Test
    fun `when restarting device then restarts device`() = runTest {
        // given
        val device = DeviceFixture.arbitrary(deviceId)

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(device.right())

        whenever(restartDevice.restartDevice(deviceId, correlationId))
            .thenReturn(Unit.right())

        // when
        val result = testee.restartDevice(adminActor, correlationId, deviceId)

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(Unit)
    }

    @Test
    fun `given invalid device id when restarting device then returns error`() = runTest {
        // given
        val error = ErrorFixture.arbitrary()

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(error.left())

        // when
        val result = testee.restartDevice(adminActor, correlationId, deviceId)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }

    @Test
    fun `given error while restarting when restarting device then returns error`() = runTest {
        // given
        val device = DeviceFixture.arbitrary(deviceId)

        val error = ErrorFixture.arbitrary()

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(device.right())

        whenever(restartDevice.restartDevice(deviceId, correlationId))
            .thenReturn(error.left())

        // when
        val result = testee.restartDevice(adminActor, correlationId, deviceId)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }
}