package cloud.fabX.fabXaccess.device.application

import FixedClock
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.ErrorFixture
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.device.model.DeviceDeleted
import cloud.fabX.fabXaccess.device.model.DeviceFixture
import cloud.fabX.fabXaccess.device.model.DeviceIdFixture
import cloud.fabX.fabXaccess.device.model.DeviceRepository
import cloud.fabX.fabXaccess.user.model.AdminFixture
import isLeft
import isRight
import kotlinx.coroutines.test.runTest
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.whenever


@OptIn(ExperimentalTime::class)
@MockitoSettings
internal class DeletingDeviceTest {

    private val adminActor = AdminFixture.arbitrary()
    private val correlationId = CorrelationIdFixture.arbitrary()

    private val deviceId = DeviceIdFixture.arbitrary()

    private val fixedInstant = Clock.System.now()
    private val fixedClock = FixedClock(fixedInstant)

    private lateinit var logger: Logger
    private lateinit var deviceRepository: DeviceRepository

    private lateinit var testee: DeletingDevice

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock deviceRepository: DeviceRepository
    ) {
        this.logger = logger
        this.deviceRepository = deviceRepository

        testee = DeletingDevice({ logger }, deviceRepository, fixedClock)
    }

    @Test
    fun `given device can be found when deleting device then sourcing event is created and stored`() = runTest {
        // given
        val device = DeviceFixture.arbitrary(deviceId, aggregateVersion = 42)

        val expectedSourcingEvent = DeviceDeleted(
            deviceId,
            43,
            adminActor.id,
            fixedInstant,
            correlationId
        )

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(device.right())

        whenever(deviceRepository.store(expectedSourcingEvent))
            .thenReturn(Unit.right())

        // when
        val result = testee.deleteDevice(
            adminActor,
            correlationId,
            deviceId
        )

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(Unit)
    }

    @Test
    fun `given device cannot be found when deleting device then returns error`() = runTest {
        // given
        val error = Error.DeviceNotFound("message", deviceId)

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(error.left())

        // when
        val result = testee.deleteDevice(
            adminActor,
            correlationId,
            deviceId
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }

    @Test
    fun `given sourcing event cannot be stored when deleting device then returns error`() = runTest {
        // given
        val device = DeviceFixture.arbitrary(deviceId, aggregateVersion = 1)

        val event = DeviceDeleted(
            deviceId,
            2,
            adminActor.id,
            fixedInstant,
            correlationId
        )

        val error = ErrorFixture.arbitrary()

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(device.right())

        whenever(deviceRepository.store(event))
            .thenReturn(error.left())

        // when
        val result = testee.deleteDevice(
            adminActor,
            correlationId,
            deviceId
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }
}