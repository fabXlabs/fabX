package cloud.fabX.fabXaccess.device.application

import arrow.core.right
import cloud.fabX.fabXaccess.common.model.ActorId
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.model.ToolDeleted
import cloud.fabX.fabXaccess.device.model.DeviceFixture
import cloud.fabX.fabXaccess.device.model.DeviceIdFixture
import cloud.fabX.fabXaccess.device.model.GettingDevicesByAttachedTool
import cloud.fabX.fabXaccess.tool.model.ToolIdFixture
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.eq
import org.mockito.kotlin.same
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@MockitoSettings
internal class DeviceDomainEventHandlerTest {

    private val actorId: ActorId = UserIdFixture.arbitrary()
    private val correlationId = CorrelationIdFixture.arbitrary()

    private val toolId = ToolIdFixture.arbitrary()

    private lateinit var logger: Logger
    private lateinit var gettingDevicesByAttachedTool: GettingDevicesByAttachedTool
    private lateinit var detachingTool: DetachingTool

    private lateinit var testee: DeviceDomainEventHandler

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock gettingDevicesByAttachedTool: GettingDevicesByAttachedTool,
        @Mock detachingTool: DetachingTool
    ) {
        this.logger = logger
        this.gettingDevicesByAttachedTool = gettingDevicesByAttachedTool
        this.detachingTool = detachingTool

        testee = DeviceDomainEventHandler({ logger }, detachingTool, gettingDevicesByAttachedTool)
    }

    @Test
    fun `when handling ToolDeleted then removes tool attachment`() = runTest {
        // given
        val domainEvent = ToolDeleted(
            actorId,
            Clock.System.now(),
            correlationId,
            toolId
        )

        val deviceId1 = DeviceIdFixture.arbitrary()
        val device1 = DeviceFixture.arbitrary(
            deviceId1,
            attachedTools = mapOf(2 to toolId)
        )

        val deviceId2 = DeviceIdFixture.arbitrary()
        val device2 = DeviceFixture.arbitrary(
            deviceId2,
            attachedTools = mapOf(3 to toolId, 4 to toolId)
        )

        whenever(gettingDevicesByAttachedTool.getByAttachedTool(toolId))
            .thenReturn(setOf(device1, device2))

        whenever(detachingTool.detachTool(domainEvent, deviceId1, 2))
            .thenReturn(Unit.right())
        whenever(detachingTool.detachTool(domainEvent, deviceId2, 3))
            .thenReturn(Unit.right())
        whenever(detachingTool.detachTool(domainEvent, deviceId2, 4))
            .thenReturn(Unit.right())

        // when
        testee.handle(domainEvent)

        // then
        verify(detachingTool).detachTool(same(domainEvent), eq(deviceId1), eq(2))
        verify(detachingTool).detachTool(same(domainEvent), eq(deviceId2), eq(3))
        verify(detachingTool).detachTool(same(domainEvent), eq(deviceId2), eq(4))
    }
}