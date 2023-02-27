package cloud.fabX.fabXaccess.device.application

import FixedClock
import arrow.core.None
import arrow.core.left
import arrow.core.right
import arrow.core.some
import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.ErrorFixture
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.device.model.ActualFirmwareVersionChanged
import cloud.fabX.fabXaccess.device.model.DesiredFirmwareVersionChanged
import cloud.fabX.fabXaccess.device.model.DeviceFixture
import cloud.fabX.fabXaccess.device.model.DeviceIdFixture
import cloud.fabX.fabXaccess.device.model.DeviceRepository
import cloud.fabX.fabXaccess.user.model.AdminFixture
import isLeft
import isRight
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@MockitoSettings
class ChangingFirmwareVersionTest {
    private val adminActor = AdminFixture.arbitrary()
    private val correlationId = CorrelationIdFixture.arbitrary()

    private val deviceId = DeviceIdFixture.arbitrary()

    private val fixedInstant = Clock.System.now()
    private val fixedClock = FixedClock(fixedInstant)

    private lateinit var logger: Logger
    private lateinit var deviceRepository: DeviceRepository

    private lateinit var testee: ChangingFirmwareVersion

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock deviceRepository: DeviceRepository
    ) {
        this.logger = logger
        this.deviceRepository = deviceRepository

        testee = ChangingFirmwareVersion({ logger }, deviceRepository, fixedClock)
    }

    @Test
    fun `given device can be found when setting actual firmware version then sourcing event is created`() = runTest {
        // given
        val device = DeviceFixture.arbitrary(deviceId, aggregateVersion = 1)

        val actualFirmwareVersion = "42.1.2"

        val expectedSourcingEvent = ActualFirmwareVersionChanged(
            deviceId,
            2,
            deviceId,
            fixedInstant,
            correlationId,
            actualFirmwareVersion
        )

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(device.right())

        whenever(deviceRepository.store(expectedSourcingEvent))
            .thenReturn(None)

        // when
        val result = testee.setActualFirmwareVersion(
            device.asActor(),
            correlationId,
            deviceId,
            actualFirmwareVersion
        )

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(Unit)
    }

    @Test
    fun `given device cannot be found when setting actual firmware version then returns error`() = runTest {
        // given
        val device = DeviceFixture.arbitrary(deviceId, aggregateVersion = 1)

        val error = Error.DeviceNotFound("message", deviceId)

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(error.left())

        // when
        val result = testee.setActualFirmwareVersion(
            device.asActor(),
            correlationId,
            deviceId,
            "1.2.3"
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }

    @Test
    fun `given domain error when setting actual firmware version then returns error`() = runTest {
        // given
        val device = DeviceFixture.arbitrary(deviceId, aggregateVersion = 1)
        val otherDevice = DeviceFixture.arbitrary()

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(device.right())

        // when
        val result = testee.setActualFirmwareVersion(
            otherDevice.asActor(),
            correlationId,
            deviceId,
            "1.2.3"
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.DeviceNotActor("Device not actor", correlationId)
            )
    }

    @Test
    fun `given sourcing event cannot be stored when setting actual firmware version then returns error`() = runTest {
        // given
        val device = DeviceFixture.arbitrary(deviceId, aggregateVersion = 1)

        val event = ActualFirmwareVersionChanged(
            deviceId,
            2,
            deviceId,
            fixedInstant,
            correlationId,
            "1.2.3"
        )

        val error = ErrorFixture.arbitrary()

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(device.right())

        whenever(deviceRepository.store(event))
            .thenReturn(error.some())

        // when
        val result = testee.setActualFirmwareVersion(
            device.asActor(),
            correlationId,
            deviceId,
            "1.2.3"
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }

    @Test
    fun `given device can be found when changing desired firmware version then sourcing event is created`() = runTest {
        // given
        val device = DeviceFixture.arbitrary(deviceId, aggregateVersion = 1)

        val desiredFirmwareVersion = "42.1.2"

        val expectedSourcingEvent = DesiredFirmwareVersionChanged(
            deviceId,
            2,
            adminActor.id,
            fixedInstant,
            correlationId,
            desiredFirmwareVersion
        )

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(device.right())

        whenever(deviceRepository.store(expectedSourcingEvent))
            .thenReturn(None)

        // when
        val result = testee.changeDesiredFirmwareVersion(
            adminActor,
            correlationId,
            deviceId,
            desiredFirmwareVersion
        )

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(Unit)
    }

    @Test
    fun `given device cannot be found when changing desired firmware version then returns error`() = runTest {
        // given
        val error = Error.DeviceNotFound("message", deviceId)

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(error.left())

        // when
        val result = testee.changeDesiredFirmwareVersion(
            adminActor,
            correlationId,
            deviceId,
            "1.2.3"
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }

    @Test
    fun `given sourcing event cannot be stored when changing desired firmware version then returns error`() = runTest {
        // given
        val device = DeviceFixture.arbitrary(deviceId, aggregateVersion = 1)

        val desiredFirmwareVersion = "42.1.2"

        val expectedSourcingEvent = DesiredFirmwareVersionChanged(
            deviceId,
            2,
            adminActor.id,
            fixedInstant,
            correlationId,
            desiredFirmwareVersion
        )

        val error = ErrorFixture.arbitrary()

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(device.right())

        whenever(deviceRepository.store(expectedSourcingEvent))
            .thenReturn(error.some())

        // when
        val result = testee.changeDesiredFirmwareVersion(
            adminActor,
            correlationId,
            deviceId,
            desiredFirmwareVersion
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }
}