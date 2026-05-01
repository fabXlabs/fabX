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
import cloud.fabX.fabXaccess.device.model.InputAttached
import cloud.fabX.fabXaccess.device.model.InputDescription
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
internal class AttachingInputTest {

    private val adminActor = AdminFixture.arbitrary()
    private val correlationId = CorrelationIdFixture.arbitrary()

    private val deviceId = DeviceIdFixture.arbitrary()

    private val fixedInstant = Clock.System.now()
    private val fixedClock = FixedClock(fixedInstant)

    private lateinit var logger: Logger
    private lateinit var deviceRepository: DeviceRepository

    private lateinit var testee: AttachingInput

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock deviceRepository: DeviceRepository
    ) {
        this.logger = logger
        this.deviceRepository = deviceRepository

        testee = AttachingInput({ logger }, deviceRepository, fixedClock)
    }

    @Test
    fun `given device can be found when attaching input then sourcing event is created and stored`() = runTest {
        // given
        val device = DeviceFixture.arbitrary(deviceId, aggregateVersion = 1)

        val pin = 4
        val name = "input name"
        val descriptionLow = "description low"
        val descriptionHigh = "description high"
        val colourLow = "#aabbcc"
        val colourHigh = "#ddeeff"

        val expectedSourcingEvent = InputAttached(
            deviceId,
            2,
            adminActor.id,
            fixedInstant,
            correlationId,
            pin,
            name,
            descriptionLow,
            descriptionHigh,
            colourLow,
            colourHigh
        )

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(device.right())

        whenever(deviceRepository.store(expectedSourcingEvent))
            .thenReturn(Unit.right())

        // when
        val result = testee.attachInput(
            adminActor,
            correlationId,
            deviceId,
            pin,
            name,
            descriptionLow,
            descriptionHigh,
            colourLow,
            colourHigh
        )

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(Unit)
    }

    @Test
    fun `given domain error when attaching input then returns domain error`() = runTest {
        // given
        val pin = 7
        val name = "new input name"
        val descriptionLow = "new description low"
        val descriptionHigh = "new description high"
        val colourLow = "#aabb12"
        val colourHigh = "#ddee34"

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

        val expectedDomainError = Error.InputPinInUse("Input already attached at pin $pin.", pin, correlationId)

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(device.right())

        // when
        val result = testee.attachInput(
            adminActor,
            correlationId,
            deviceId,
            pin,
            name,
            descriptionLow,
            descriptionHigh,
            colourLow,
            colourHigh
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(expectedDomainError)
    }

    @Test
    fun `given sourcing event cannot be stored when attaching tool then returns error`() = runTest {
        val device = DeviceFixture.arbitrary(deviceId, aggregateVersion = 1)

        val pin = 4
        val name = "input name"
        val descriptionLow = "description low"
        val descriptionHigh = "description high"
        val colourLow = "#aabbcc"
        val colourHigh = "#ddeeff"

        val expectedSourcingEvent = InputAttached(
            deviceId,
            2,
            adminActor.id,
            fixedInstant,
            correlationId,
            pin,
            name,
            descriptionLow,
            descriptionHigh,
            colourLow,
            colourHigh
        )

        val error = ErrorFixture.arbitrary()

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(device.right())

        whenever(deviceRepository.store(expectedSourcingEvent))
            .thenReturn(error.left())

        // when
        val result = testee.attachInput(
            adminActor,
            correlationId,
            deviceId,
            pin,
            name,
            descriptionLow,
            descriptionHigh,
            colourLow,
            colourHigh
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }
}