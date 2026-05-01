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
import cloud.fabX.fabXaccess.device.model.DeviceFixture
import cloud.fabX.fabXaccess.device.model.DeviceIdFixture
import cloud.fabX.fabXaccess.device.model.DeviceRepository
import cloud.fabX.fabXaccess.device.model.InputDescription
import cloud.fabX.fabXaccess.device.model.InputDetached
import cloud.fabX.fabXaccess.user.model.AdminFixture
import isLeft
import isRight
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.whenever

@OptIn(ExperimentalTime::class)
@MockitoSettings
internal class DetachingInputTest {

    private val adminActor = AdminFixture.arbitrary()
    private val correlationId = CorrelationIdFixture.arbitrary()

    private val deviceId = DeviceIdFixture.arbitrary()

    private val fixedInstant = Clock.System.now()
    private val fixedClock = FixedClock(fixedInstant)

    private lateinit var logger: Logger
    private lateinit var deviceRepository: DeviceRepository

    private lateinit var testee: DetachingInput

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock deviceRepository: DeviceRepository
    ) {
        this.logger = logger
        this.deviceRepository = deviceRepository

        testee = DetachingInput({ logger }, deviceRepository, fixedClock)
    }

    @Test
    fun `when detaching input then sourcing event is created and stored`() = runTest {
        // given
        val pin = 3

        val device = DeviceFixture.arbitrary(
            deviceId,
            aggregateVersion = 1,
            inputDescriptions = mapOf(
                pin to InputDescription(
                    "input name",
                    "description low",
                    "description high",
                    "#aabbcc",
                    "#ddeeff"
                )
            )
        )

        val expectedSourcingEvent = InputDetached(
            deviceId,
            2,
            adminActor.id,
            fixedInstant,
            correlationId,
            pin
        )

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(device.right())

        whenever(deviceRepository.store(expectedSourcingEvent))
            .thenReturn(Unit.right())

        // when
        val result = testee.detachInput(
            adminActor,
            correlationId,
            deviceId,
            pin
        )

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(Unit)
    }

    @Test
    fun `given domain error when detaching tool then returns domain error`() = runTest {
        // given
        val device = DeviceFixture.arbitrary(
            deviceId,
            aggregateVersion = 1,
            inputDescriptions = mapOf()
        )

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(device.right())

        val pin = 7
        val expectedDomainError =
            Error.InputPinNotInUse("No input attached at pin $pin.", pin, correlationId)

        // when
        val result = testee.detachInput(
            adminActor,
            correlationId,
            deviceId,
            pin
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(expectedDomainError)
    }

    @Test
    fun `given sourcing event cannot be stored when attaching tool then returns error`() = runTest {
        // given
        val pin = 3
        val device = DeviceFixture.arbitrary(
            deviceId,
            aggregateVersion = 1,
            inputDescriptions = mapOf(
                pin to InputDescription(
                    "input name",
                    "description low",
                    "description high",
                    "#aabbcc",
                    "#ddeeff"
                )
            )
        )

        val expectedSourcingEvent = InputDetached(
            deviceId,
            2,
            adminActor.id,
            fixedInstant,
            correlationId,
            pin
        )

        val error = ErrorFixture.arbitrary()

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(device.right())

        whenever(deviceRepository.store(expectedSourcingEvent))
            .thenReturn(error.left())

        // when
        val result = testee.detachInput(
            adminActor,
            correlationId,
            deviceId,
            pin
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }
}