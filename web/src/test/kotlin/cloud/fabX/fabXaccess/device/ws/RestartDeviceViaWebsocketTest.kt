package cloud.fabX.fabXaccess.device.ws

import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.ErrorFixture
import cloud.fabX.fabXaccess.device.model.DeviceIdFixture
import isLeft
import isRight
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.capture
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@MockitoSettings
internal class RestartDeviceViaWebsocketTest {
    private val deviceId = DeviceIdFixture.arbitrary()
    private val correlationId = CorrelationIdFixture.arbitrary()

    private lateinit var deviceWebsocketController: DeviceWebsocketController
    private lateinit var testee: RestartDeviceViaWebsocket

    @Captor
    private lateinit var commandCaptor: ArgumentCaptor<RestartDevice>

    @Captor
    private lateinit var responseCommandIdCaptor: ArgumentCaptor<Int>

    @BeforeEach
    fun `configure WebModule`(
        @Mock deviceWebsocketController: DeviceWebsocketController
    ) {
        this.deviceWebsocketController = deviceWebsocketController

        testee = RestartDeviceViaWebsocket(deviceWebsocketController)
    }

    @Test
    fun `when restarting device then sends command and receives response`() = runTest {
        // given
        val deviceResponse = DeviceRestartResponse(123)

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
                any<RestartDevice>(),
                eq(correlationId)
            )
        ).thenReturn(Unit.right())

        whenever(
            deviceWebsocketController.receiveDeviceResponse(
                eq(deviceId),
                any(),
                eq(correlationId),
                any()
            )
        ).thenReturn(deviceResponse.right())

        // when
        val result = testee.restartDevice(deviceId, correlationId)

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(Unit)

        verify(deviceWebsocketController).sendCommand(any(), capture(commandCaptor), any())
        verify(deviceWebsocketController).receiveDeviceResponse(any(), capture(responseCommandIdCaptor), any(), any())
        assertThat(responseCommandIdCaptor.value).isEqualTo(commandCaptor.value.commandId)
    }

    @Test
    fun `given error while setting up receiving response when restarting device then returns error`() = runTest {
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
        val result = testee.restartDevice(deviceId, correlationId)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }

    @Test
    fun `given error while sending command when restarting device then returns error`() = runTest {
        // given
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
                any<RestartDevice>(),
                eq(correlationId)
            )
        ).thenReturn(error.left())

        // when
        val result = testee.restartDevice(deviceId, correlationId)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }

    @Test
    fun `given error while receiving response when restarting device then returns error`() = runTest {
        // given
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
                any<RestartDevice>(),
                eq(correlationId)
            )
        ).thenReturn(Unit.right())

        whenever(
            deviceWebsocketController.receiveDeviceResponse(
                eq(deviceId),
                any(),
                eq(correlationId),
                any()
            )
        ).thenReturn(error.left())

        // when
        val result = testee.restartDevice(deviceId, correlationId)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }

    @Test
    fun `given unexpected device response when restarting device then returns error`() = runTest {
        // given
        val deviceResponse = ErrorResponse(123, "msg", mapOf(), null)

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
                any<RestartDevice>(),
                eq(correlationId)
            )
        ).thenReturn(Unit.right())

        whenever(
            deviceWebsocketController.receiveDeviceResponse(
                eq(deviceId),
                any(),
                eq(correlationId),
                any()
            )
        ).thenReturn(deviceResponse.right())

        // when
        val result = testee.restartDevice(deviceId, correlationId)

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