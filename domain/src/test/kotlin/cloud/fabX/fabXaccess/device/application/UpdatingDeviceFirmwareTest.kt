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
import cloud.fabX.fabXaccess.device.model.UpdateDeviceFirmware
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
internal class UpdatingDeviceFirmwareTest {

    private val adminActor = AdminFixture.arbitrary()
    private val deviceId = DeviceIdFixture.arbitrary()
    private val correlationId = CorrelationIdFixture.arbitrary()

    private lateinit var logger: Logger
    private lateinit var deviceRepository: DeviceRepository
    private lateinit var updateDeviceFirmware: UpdateDeviceFirmware

    private lateinit var testee: UpdatingDeviceFirmware

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock deviceRepository: DeviceRepository,
        @Mock updateDeviceFirmware: UpdateDeviceFirmware
    ) {
        this.logger = logger
        this.deviceRepository = deviceRepository
        this.updateDeviceFirmware = updateDeviceFirmware

        testee = UpdatingDeviceFirmware({ logger }, deviceRepository, updateDeviceFirmware)
    }

    @Test
    fun `when updating device firmware then updates device firmware`() = runTest {
        // given
        val device = DeviceFixture.arbitrary(deviceId)

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(device.right())

        whenever(updateDeviceFirmware.updateDeviceFirmware(deviceId, correlationId))
            .thenReturn(Unit.right())

        // when
        val result = testee.updateDeviceFirmware(adminActor, correlationId, deviceId)

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(Unit)
    }

    @Test
    fun `given invalid device id when updating device firmware then returns error`() = runTest {
        // given
        val error = ErrorFixture.arbitrary()

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(error.left())

        // when
        val result = testee.updateDeviceFirmware(adminActor, correlationId, deviceId)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }

    @Test
    fun `given error while updating when updating device firmware then returns error`() = runTest {
        // given
        val device = DeviceFixture.arbitrary(deviceId)

        val error = ErrorFixture.arbitrary()

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(device.right())

        whenever(updateDeviceFirmware.updateDeviceFirmware(deviceId, correlationId))
            .thenReturn(error.left())

        // when
        val result = testee.updateDeviceFirmware(adminActor, correlationId, deviceId)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }
}