package cloud.fabX.fabXaccess.device.application

import FixedClock
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.ErrorFixture
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.device.model.DeviceDetailsChanged
import cloud.fabX.fabXaccess.device.model.DeviceFixture
import cloud.fabX.fabXaccess.device.model.DeviceIdFixture
import cloud.fabX.fabXaccess.device.model.DeviceRepository
import cloud.fabX.fabXaccess.user.model.AdminFixture
import isLeft
import isRight
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.whenever

@MockitoSettings
internal class ChangingDeviceTest {

    private val adminActor = AdminFixture.arbitrary()
    private val correlationId = CorrelationIdFixture.arbitrary()

    private val deviceId = DeviceIdFixture.arbitrary()

    private val fixedInstant = Clock.System.now()
    private val fixedClock = FixedClock(fixedInstant)

    private lateinit var logger: Logger
    private lateinit var deviceRepository: DeviceRepository

    private lateinit var testee: ChangingDevice

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock deviceRepository: DeviceRepository
    ) {
        this.logger = logger
        this.deviceRepository = deviceRepository

        testee = ChangingDevice({ logger }, deviceRepository, fixedClock)
    }

    @Test
    fun `given device can be found when changing device details then sourcing event is created and stored`() = runTest {
        // given
        val device = DeviceFixture.arbitrary(deviceId, aggregateVersion = 1)

        val newName = ChangeableValue.ChangeToValueString("newName")
        val newBackground = ChangeableValue.ChangeToValueString("newBackground")
        val newBackupBackendUrl = ChangeableValue.ChangeToValueString("newBackupBackendUrl")

        val expectedSourcingEvent = DeviceDetailsChanged(
            deviceId,
            2,
            adminActor.id,
            fixedInstant,
            correlationId,
            newName,
            newBackground,
            newBackupBackendUrl
        )

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(device.right())

        whenever(deviceRepository.store(expectedSourcingEvent))
            .thenReturn(Unit.right())

        // when
        val result = testee.changeDeviceDetails(
            adminActor,
            correlationId,
            deviceId,
            newName,
            newBackground,
            newBackupBackendUrl
        )

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(Unit)
    }

    @Test
    fun `given device cannot be found when changing device details then returns error`() = runTest {
        // given
        val error = Error.DeviceNotFound("message", deviceId)

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(error.left())

        // when
        val result = testee.changeDeviceDetails(
            adminActor,
            correlationId,
            deviceId,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }

    @Test
    fun `given sourcing event cannot be stored when changing device details then returns error`() = runTest {
        // given
        val device = DeviceFixture.arbitrary(deviceId, aggregateVersion = 1)

        val event = DeviceDetailsChanged(
            deviceId,
            2,
            adminActor.id,
            fixedInstant,
            correlationId,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs
        )

        val error = ErrorFixture.arbitrary()

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(device.right())

        whenever(deviceRepository.store(event))
            .thenReturn(error.left())

        // when
        val result = testee.changeDeviceDetails(
            adminActor,
            correlationId,
            deviceId,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }
}