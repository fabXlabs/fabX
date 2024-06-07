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
import cloud.fabX.fabXaccess.device.model.ToolAttached
import cloud.fabX.fabXaccess.tool.model.ToolFixture
import cloud.fabX.fabXaccess.tool.model.ToolIdFixture
import cloud.fabX.fabXaccess.tool.model.ToolRepository
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
internal class AttachingToolTest {

    private val adminActor = AdminFixture.arbitrary()
    private val correlationId = CorrelationIdFixture.arbitrary()

    private val deviceId = DeviceIdFixture.arbitrary()

    private val fixedInstant = Clock.System.now()
    private val fixedClock = FixedClock(fixedInstant)

    private lateinit var logger: Logger
    private lateinit var deviceRepository: DeviceRepository
    private lateinit var toolRepository: ToolRepository

    private lateinit var testee: AttachingTool

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock deviceRepository: DeviceRepository,
        @Mock toolRepository: ToolRepository
    ) {
        this.logger = logger
        this.deviceRepository = deviceRepository
        this.toolRepository = toolRepository

        testee = AttachingTool({ logger }, deviceRepository, toolRepository, fixedClock)
    }

    @Test
    fun `given device can be found when attaching tool then sourcing event is created and stored`() = runTest {
        // given
        val device = DeviceFixture.arbitrary(deviceId, aggregateVersion = 1)

        val toolId = ToolIdFixture.arbitrary()
        val tool = ToolFixture.arbitrary(toolId)

        val pin = 42

        val expectedSourcingEvent = ToolAttached(
            deviceId,
            2,
            adminActor.id,
            fixedInstant,
            correlationId,
            pin,
            toolId
        )

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(device.right())

        whenever(toolRepository.getToolById(toolId))
            .thenReturn(tool.right())

        whenever(deviceRepository.store(expectedSourcingEvent))
            .thenReturn(Unit.right())

        // when
        val result = testee.attachTool(
            adminActor,
            correlationId,
            deviceId,
            pin,
            toolId
        )

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(Unit)
    }

    @Test
    fun `given domain error when attaching tool then returns domain error`() = runTest {
        // given
        val device = DeviceFixture.arbitrary(deviceId, aggregateVersion = 1)

        val toolId = ToolIdFixture.arbitrary()

        val expectedDomainError = ErrorFixture.arbitrary()

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(device.right())

        whenever(toolRepository.getToolById(toolId))
            .thenReturn(expectedDomainError.left())

        // when
        val result = testee.attachTool(
            adminActor,
            correlationId,
            deviceId,
            1,
            toolId
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(expectedDomainError)
    }

    @Test
    fun `given device cannot be found when attaching tool then returns error`() = runTest {
        // given
        val error = Error.DeviceNotFound("", deviceId)

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(error.left())

        // when
        val result = testee.attachTool(
            adminActor,
            correlationId,
            deviceId,
            1,
            ToolIdFixture.arbitrary()
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }

    @Test
    fun `given sourcing event cannot be stored when attaching tool then returns error`() = runTest {
        val device = DeviceFixture.arbitrary(deviceId, aggregateVersion = 1)

        val toolId = ToolIdFixture.arbitrary()
        val tool = ToolFixture.arbitrary(toolId)

        val pin = 42

        val expectedSourcingEvent = ToolAttached(
            deviceId,
            2,
            adminActor.id,
            fixedInstant,
            correlationId,
            pin,
            toolId
        )

        val error = ErrorFixture.arbitrary()

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(device.right())

        whenever(toolRepository.getToolById(toolId))
            .thenReturn(tool.right())

        whenever(deviceRepository.store(expectedSourcingEvent))
            .thenReturn(error.left())

        // when
        val result = testee.attachTool(
            adminActor,
            correlationId,
            deviceId,
            pin,
            toolId
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }
}