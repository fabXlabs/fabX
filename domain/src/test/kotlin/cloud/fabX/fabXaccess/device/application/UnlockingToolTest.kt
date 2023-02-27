package cloud.fabX.fabXaccess.device.application

import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.ErrorFixture
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.model.TaggedCounter
import cloud.fabX.fabXaccess.common.model.ToolId
import cloud.fabX.fabXaccess.device.model.DeviceFixture
import cloud.fabX.fabXaccess.device.model.DeviceIdFixture
import cloud.fabX.fabXaccess.device.model.DeviceRepository
import cloud.fabX.fabXaccess.device.model.UnlockToolAtDevice
import cloud.fabX.fabXaccess.tool.model.GettingToolById
import cloud.fabX.fabXaccess.tool.model.ToolFixture
import cloud.fabX.fabXaccess.tool.model.ToolIdFixture
import cloud.fabX.fabXaccess.tool.model.ToolType
import cloud.fabX.fabXaccess.user.model.AdminFixture
import isLeft
import isRight
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@MockitoSettings
internal class UnlockingToolTest {

    private val adminActor = AdminFixture.arbitrary()
    private val deviceId = DeviceIdFixture.arbitrary()
    private val toolId = ToolIdFixture.arbitrary()
    private val correlationId = CorrelationIdFixture.arbitrary()

    private lateinit var logger: Logger
    private lateinit var deviceRepository: DeviceRepository
    private lateinit var toolRepository: GettingToolById
    private lateinit var unlockingToolAtDevice: UnlockToolAtDevice
    private lateinit var toolUsageCounter: TaggedCounter<ToolId>

    private lateinit var testee: UnlockingTool

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock deviceRepository: DeviceRepository,
        @Mock toolRepository: GettingToolById,
        @Mock unlockToolAtDevice: UnlockToolAtDevice,
        @Mock toolUsageCounter: TaggedCounter<ToolId>
    ) {
        this.logger = logger
        this.deviceRepository = deviceRepository
        this.toolRepository = toolRepository
        this.unlockingToolAtDevice = unlockToolAtDevice
        this.toolUsageCounter = toolUsageCounter

        testee = UnlockingTool({ logger }, deviceRepository, toolRepository, unlockToolAtDevice, toolUsageCounter)
    }

    @Test
    fun `when unlocking tool then unlocks tool`() = runTest {
        // given
        val device = DeviceFixture.arbitrary(
            deviceId,
            attachedTools = mapOf(
                1 to toolId
            )
        )

        val tool = ToolFixture.arbitrary(toolId, type = ToolType.UNLOCK)

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(device.right())

        whenever(toolRepository.getToolById(toolId))
            .thenReturn(tool.right())

        whenever(unlockingToolAtDevice.unlockTool(deviceId, toolId, correlationId))
            .thenReturn(Unit.right())

        // when
        val result = testee.unlockTool(adminActor, correlationId, deviceId, toolId)

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(Unit)
    }

    @Test
    fun `given invalid device id when unlocking tool then returns error`() = runTest {
        // given
        val error = ErrorFixture.arbitrary()

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(error.left())

        // when
        val result = testee.unlockTool(adminActor, correlationId, deviceId, toolId)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }

    @Test
    fun `given tool not attached to device when unlocking tool then returns error`() = runTest {
        // given
        val device = DeviceFixture.arbitrary(
            deviceId,
            attachedTools = mapOf()
        )

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(device.right())

        // when
        val result = testee.unlockTool(adminActor, correlationId, deviceId, toolId)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.ToolNotAttachedToDevice(
                    "Tool $toolId not attached to device $deviceId.",
                    deviceId,
                    toolId,
                    correlationId
                )
            )
    }

    @Test
    fun `given invalid tool id when unlocking tool then returns error`() = runTest {
        // given
        val device = DeviceFixture.arbitrary(
            deviceId,
            attachedTools = mapOf(
                1 to toolId
            )
        )

        val error = ErrorFixture.arbitrary()

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(device.right())

        whenever(toolRepository.getToolById(toolId))
            .thenReturn(error.left())

        // when
        val result = testee.unlockTool(adminActor, correlationId, deviceId, toolId)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }

    @Test
    fun `given error while unlocking when unlocking tool then returns error`() = runTest {
        // given
        val device = DeviceFixture.arbitrary(
            deviceId,
            attachedTools = mapOf(
                1 to toolId
            )
        )

        val tool = ToolFixture.arbitrary(toolId, type = ToolType.UNLOCK)

        val error = ErrorFixture.arbitrary()

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(device.right())

        whenever(toolRepository.getToolById(toolId))
            .thenReturn(tool.right())

        whenever(unlockingToolAtDevice.unlockTool(deviceId, toolId, correlationId))
            .thenReturn(error.left())

        // when
        val result = testee.unlockTool(adminActor, correlationId, deviceId, toolId)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }
}