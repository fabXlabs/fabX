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
import cloud.fabX.fabXaccess.common.model.ToolDeleted
import cloud.fabX.fabXaccess.device.model.DeviceFixture
import cloud.fabX.fabXaccess.device.model.DeviceIdFixture
import cloud.fabX.fabXaccess.device.model.DeviceRepository
import cloud.fabX.fabXaccess.device.model.ToolDetached
import cloud.fabX.fabXaccess.tool.model.ToolIdFixture
import cloud.fabX.fabXaccess.user.model.AdminFixture
import cloud.fabX.fabXaccess.user.model.UserIdFixture
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
internal class DetachingToolTest {

    private val adminActor = AdminFixture.arbitrary()
    private val correlationId = CorrelationIdFixture.arbitrary()

    private val deviceId = DeviceIdFixture.arbitrary()

    private val fixedInstant = Clock.System.now()
    private val fixedClock = FixedClock(fixedInstant)

    private lateinit var logger: Logger
    private lateinit var deviceRepository: DeviceRepository

    private lateinit var testee: DetachingTool

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock deviceRepository: DeviceRepository
    ) {
        this.logger = logger
        this.deviceRepository = deviceRepository

        testee = DetachingTool({ logger }, deviceRepository, fixedClock)
    }

    @Test
    fun `given device can be found when detaching tool then sourcing event is created and stored`() = runTest {
        // given
        val toolId = ToolIdFixture.arbitrary()

        val pin = 3
        val device = DeviceFixture.arbitrary(
            deviceId,
            aggregateVersion = 1,
            attachedTools = mapOf(pin to toolId)
        )

        val expectedSourcingEvent = ToolDetached(
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
        val result = testee.detachTool(
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
    fun `given triggered by domain event when detaching tool then sourcing event is created and stored`() = runTest {
        val toolId = ToolIdFixture.arbitrary()

        val pin = 3
        val device = DeviceFixture.arbitrary(
            deviceId,
            aggregateVersion = 1,
            attachedTools = mapOf(pin to toolId)
        )

        val actorId = UserIdFixture.arbitrary()

        val domainEvent = ToolDeleted(
            actorId,
            Clock.System.now(),
            correlationId,
            toolId
        )

        val expectedSourcingEvent = ToolDetached(
            deviceId,
            2,
            actorId,
            fixedInstant,
            correlationId,
            pin
        )

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(device.right())

        whenever(deviceRepository.store(expectedSourcingEvent))
            .thenReturn(Unit.right())

        // when
        val result = testee.detachTool(
            domainEvent,
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
            attachedTools = mapOf()
        )

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(device.right())

        val pin = 42
        val expectedDomainError =
            Error.PinNotInUse("No tool attached at pin $pin.", pin, correlationId)

        // when
        val result = testee.detachTool(
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
    fun `given tool cannot be found when detaching tool then returns error`() = runTest {
        // given
        val error = Error.DeviceNotFound("", deviceId)

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(error.left())

        // when
        val result = testee.detachTool(
            adminActor,
            correlationId,
            deviceId,
            42
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }

    @Test
    fun `given sourcing event cannot be stored when attaching tool then returns error`() = runTest {
        // given
        val toolId = ToolIdFixture.arbitrary()

        val pin = 3
        val device = DeviceFixture.arbitrary(
            deviceId,
            aggregateVersion = 1,
            attachedTools = mapOf(pin to toolId)
        )

        val expectedSourcingEvent = ToolDetached(
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
        val result = testee.detachTool(
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