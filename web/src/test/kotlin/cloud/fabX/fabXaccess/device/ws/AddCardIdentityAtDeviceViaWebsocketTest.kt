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
import org.mockito.kotlin.argThat
import org.mockito.kotlin.capture
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@MockitoSettings
internal class AddCardIdentityAtDeviceViaWebsocketTest {
    private val deviceId = DeviceIdFixture.arbitrary()
    private val correlationId = CorrelationIdFixture.arbitrary()

    private lateinit var deviceWebsocketController: DeviceWebsocketController
    private lateinit var testee: AddCardIdentityAtDeviceViaWebsocket

    @Captor
    private lateinit var commandCaptor: ArgumentCaptor<CreateCard>

    @Captor
    private lateinit var responseCommandIdCaptor: ArgumentCaptor<Int>

    @BeforeEach
    fun `configure WebModule`(
        @Mock deviceWebsocketController: DeviceWebsocketController
    ) {
        this.deviceWebsocketController = deviceWebsocketController

        testee = AddCardIdentityAtDeviceViaWebsocket(deviceWebsocketController)
    }

    @Test
    fun `when adding card identity at device then sends command and receives response`() = runTest {
        // given
        val commandId = 1234
        val userName = "some body"
        val cardId = "AABBCC11223344"
        val cardSecret = "EE334F5E740985180C9EDAA6B5A9EB159CFB4F19427C68336D6D23D5015547CE"
        val deviceResponse = CardCreationResponse(commandId, cardId)

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
                eq(correlationId),
                timeoutMs = eq(15_000L)
            )
        ).thenReturn(deviceResponse.right())

        // when
        val result = testee.createCard(deviceId, correlationId, userName, cardSecret)

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(cardId)

        verify(deviceWebsocketController).sendCommand(any(), capture(commandCaptor), any())
        assertThat(commandCaptor.value.userName).isEqualTo(userName)
        assertThat(commandCaptor.value.cardSecret).isEqualTo(cardSecret)

        verify(deviceWebsocketController).receiveDeviceResponse(any(), capture(responseCommandIdCaptor), any(), eq(15_000L))
        assertThat(responseCommandIdCaptor.value).isEqualTo(commandCaptor.value.commandId)
    }

    @Test
    fun `given error while setting up receiving response when adding card identity then returns error`() = runTest {
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
        val result = testee.createCard(deviceId, correlationId, "some body", "s3cret")

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }

    @Test
    fun `given error while sending command when adding card identity then returns error`() = runTest {
        // given
        val userName = "some body"
        val cardSecret = "EE334F5E740985180C9EDAA6B5A9EB159CFB4F19427C68336D6D23D5015547CE"


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
                    this is CreateCard && this.userName == userName && this.cardSecret == cardSecret
                },
                eq(correlationId)
            )
        ).thenReturn(error.left())

        // when
        val result = testee.createCard(deviceId, correlationId, userName, cardSecret)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }

    @Test
    fun `given error while receiving response when adding card identity then returns error`() = runTest {
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
                any(),
                eq(correlationId)
            )
        ).thenReturn(Unit.right())

        whenever(
            deviceWebsocketController.receiveDeviceResponse(
                eq(deviceId),
                any(),
                eq(correlationId),
                eq(15_000L)
            )
        ).thenReturn(error.left())

        // when
        val result = testee.createCard(deviceId, correlationId, "some body", "s3cret")

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }

    @Test
    fun `given unexpected device response when adding card identity then returns error`() = runTest {
        // given
        val commandId = 1234
        val userName = "some body"
        val cardSecret = "EE334F5E740985180C9EDAA6B5A9EB159CFB4F19427C68336D6D23D5015547CE"
        val deviceResponse = ErrorResponse(commandId, "msg", mapOf(), null)

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
                eq(correlationId),
                eq(15_000L)
            )
        ).thenReturn(deviceResponse.right())

        // when
        val result = testee.createCard(deviceId, correlationId, userName, cardSecret)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.UnexpectedDeviceResponse(
                    "Unexpected device response type.",
                    deviceId,
                    "ErrorResponse(commandId=$commandId, message=msg, parameters={}, correlationId=null)",
                    correlationId
                )
            )
    }
}