package cloud.fabX.fabXaccess.device.ws

import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.ErrorFixture
import cloud.fabX.fabXaccess.device.model.DeviceIdFixture
import cloud.fabX.fabXaccess.tool.model.ToolIdFixture
import isLeft
import isRight
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.capture
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@MockitoSettings
internal class UnlockToolAtDeviceViaWebsocketTest {
    private val deviceId = DeviceIdFixture.arbitrary()
    private val toolId = ToolIdFixture.arbitrary()
    private val correlationId = CorrelationIdFixture.arbitrary()

    private lateinit var deviceWebsocketController: DeviceWebsocketController
    private lateinit var testee: UnlockToolAtDeviceViaWebsocket

    @Captor
    private lateinit var commandCaptor: ArgumentCaptor<UnlockTool>

    @Captor
    private lateinit var responseCommandIdCaptor: ArgumentCaptor<Long>

    @BeforeEach
    fun `configure WebModule`(
        @Mock deviceWebsocketController: DeviceWebsocketController
    ) {
        this.deviceWebsocketController = deviceWebsocketController

        testee = UnlockToolAtDeviceViaWebsocket(deviceWebsocketController)
    }

    @Test
    fun `when unlocking tool then sends command and receives response`() = runTest {
        // given
        val deviceResponse = ToolUnlockResponse(123L)

        whenever(
            deviceWebsocketController.setupReceivingDeviceResponse(
                eq(deviceId),
                any(),
                eq(correlationId)
            )
        ).thenReturn(Unit.right())

        whenever(
            deviceWebsocketController.sendCommand(
                eq(deviceId),
                any(),
                eq(correlationId)
            )
        ).thenReturn(Unit.right())

        whenever(
            deviceWebsocketController.receiveDeviceResponse(
                eq(deviceId),
                any(),
                eq(correlationId)
            )
        ).thenReturn(deviceResponse.right())

        // when
        val result = testee.unlockTool(deviceId, toolId, correlationId)

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(Unit)

        verify(deviceWebsocketController).sendCommand(any(), capture(commandCaptor), any())
        assertThat(commandCaptor.value.toolId).isEqualTo(toolId.serialize())

        verify(deviceWebsocketController).receiveDeviceResponse(any(), capture(responseCommandIdCaptor), any())
        assertThat(responseCommandIdCaptor.value).isEqualTo(commandCaptor.value.commandId)
    }

    @Test
    fun `given error while setting up receiving response when unlocking tool then returns error`() = runTest {
        // given
        val error = ErrorFixture.arbitrary()

        whenever(
            deviceWebsocketController.setupReceivingDeviceResponse(
                eq(deviceId),
                any(),
                eq(correlationId)
            )
        ).thenReturn(error.left())

        // when
        val result = testee.unlockTool(deviceId, toolId, correlationId)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }

    @Test
    fun `given error while sending command when unlocking tool then returns error`() = runTest {
        // given
        val tId = toolId

        val error = Error.DeviceNotConnected("msg", deviceId, correlationId)

        whenever(
            deviceWebsocketController.setupReceivingDeviceResponse(
                eq(deviceId),
                any(),
                eq(correlationId)
            )
        ).thenReturn(Unit.right())

        whenever(
            deviceWebsocketController.sendCommand(
                eq(deviceId),
                argThat {
                    this is UnlockTool && this.toolId == tId.serialize()
                },
                eq(correlationId)
            )
        ).thenReturn(error.left())

        // when
        val result = testee.unlockTool(deviceId, toolId, correlationId)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }

    @Test
    fun `given error while receiving response when unlocking tool then returns error`() = runTest {
        // given
        val tId = toolId

        val error = Error.DeviceTimeout("msg", deviceId, correlationId)

        whenever(
            deviceWebsocketController.setupReceivingDeviceResponse(
                eq(deviceId),
                any(),
                eq(correlationId)
            )
        ).thenReturn(Unit.right())

        whenever(
            deviceWebsocketController.sendCommand(
                eq(deviceId),
                argThat {
                    this is UnlockTool && this.toolId == tId.serialize()
                },
                eq(correlationId)
            )
        ).thenReturn(Unit.right())

        whenever(
            deviceWebsocketController.receiveDeviceResponse(
                eq(deviceId),
                any(),
                eq(correlationId)
            )
        ).thenReturn(error.left())

        // when
        val result = testee.unlockTool(deviceId, toolId, correlationId)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }

    @Test
    fun `given unexpected device response when unlocking tool then returns error`() = runTest {
        // given
        val tId = toolId

        val deviceResponse = ErrorResponse(123L, "msg", mapOf(), null)

        whenever(
            deviceWebsocketController.setupReceivingDeviceResponse(
                eq(deviceId),
                any(),
                eq(correlationId)
            )
        ).thenReturn(Unit.right())

        whenever(
            deviceWebsocketController.sendCommand(
                eq(deviceId),
                argThat {
                    this is UnlockTool && this.toolId == tId.serialize()
                },
                eq(correlationId)
            )
        ).thenReturn(Unit.right())

        whenever(
            deviceWebsocketController.receiveDeviceResponse(
                eq(deviceId),
                any(),
                eq(correlationId)
            )
        ).thenReturn(deviceResponse.right())

        // when
        val result = testee.unlockTool(deviceId, toolId, correlationId)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.UnexpectedDeviceResponse(
                    "Unexpected device response type.",
                    deviceId,
                    "ErrorResponse(commandId=123, message=msg, parameters={}, correlationId=null)",
                    correlationId
                )
            )
    }
}