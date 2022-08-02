package cloud.fabX.fabXaccess.device.ws

import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.ErrorFixture
import cloud.fabX.fabXaccess.device.model.DeviceFixture
import cloud.fabX.fabXaccess.tool.model.ToolIdFixture
import cloud.fabX.fabXaccess.user.application.LoggingUnlockedTool
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.rest.AuthenticationService
import cloud.fabX.fabXaccess.user.rest.CardIdentity
import cloud.fabX.fabXaccess.user.rest.PhoneNrIdentity
import isLeft
import isRight
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@MockitoSettings
internal class DeviceNotificationHandlerImplTest {
    private lateinit var loggingUnlockedTool: LoggingUnlockedTool
    private lateinit var authenticationService: AuthenticationService

    private lateinit var notificationHandler: DeviceNotificationHandler

    @BeforeEach
    fun `configure WebModule`(
        @Mock loggingUnlockedTool: LoggingUnlockedTool,
        @Mock authenticationService: AuthenticationService
    ) {
        this.loggingUnlockedTool = loggingUnlockedTool
        this.authenticationService = authenticationService
        notificationHandler = DeviceNotificationHandlerImpl(loggingUnlockedTool, authenticationService)
    }

    @Test
    fun `when handling ToolUnlockedNotification then logs unlocked tool`() = runTest {
        // given
        val device = DeviceFixture.arbitrary()

        val user = UserFixture.arbitrary()

        val toolId = ToolIdFixture.arbitrary()

        val phoneNrIdentity = PhoneNrIdentity("+49123456789")

        val cardId = "11223344556677"
        val cardSecret = "EE334F5E740985180C9EDAA6B5A9EB159CFB4F19427C68336D6D23D5015547CE"
        val cardIdentity = CardIdentity(cardId, cardSecret)

        val notification = ToolUnlockedNotification(toolId.serialize(), phoneNrIdentity, cardIdentity)

        val deviceActorOnBehalfOfUser = device.asActor().copy(onBehalfOf = user.asMember())

        whenever(
            authenticationService.augmentDeviceActorOnBehalfOfUser(
                eq(device.asActor()),
                eq(cardIdentity),
                eq(phoneNrIdentity),
                any()
            )
        ).thenReturn(deviceActorOnBehalfOfUser.right())

        whenever(loggingUnlockedTool.logUnlockedTool(eq(deviceActorOnBehalfOfUser), eq(toolId), any()))
            .thenReturn(Unit.right())

        // when
        val result = notificationHandler.handle(device.asActor(), notification)

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(Unit)
    }

    @Test
    fun `given error augmenting device actor on behalf of user when handling ToolUnlockedNotification then returns error`() =
        runTest {
            // given
            val device = DeviceFixture.arbitrary()

            val toolId = ToolIdFixture.arbitrary()

            val phoneNrIdentity = PhoneNrIdentity("+49123456789")

            val notification = ToolUnlockedNotification(toolId.serialize(), phoneNrIdentity, null)

            val error = ErrorFixture.arbitrary()

            whenever(
                authenticationService.augmentDeviceActorOnBehalfOfUser(
                    eq(device.asActor()),
                    isNull(),
                    eq(phoneNrIdentity),
                    any()
                )
            ).thenReturn(error.left())

            // when
            val result = notificationHandler.handle(device.asActor(), notification)

            // then
            assertThat(result)
                .isLeft()
                .isEqualTo(error)
        }

    @Test
    fun `given error logging unlocked tool when handling ToolUnlockedNotification then returns error`() = runTest {
        // given
        val device = DeviceFixture.arbitrary()

        val user = UserFixture.arbitrary()

        val toolId = ToolIdFixture.arbitrary()

        val phoneNrIdentity = PhoneNrIdentity("+49123456789")

        val cardId = "11223344556677"
        val cardSecret = "EE334F5E740985180C9EDAA6B5A9EB159CFB4F19427C68336D6D23D5015547CE"
        val cardIdentity = CardIdentity(cardId, cardSecret)

        val notification = ToolUnlockedNotification(toolId.serialize(), phoneNrIdentity, cardIdentity)

        val deviceActorOnBehalfOfUser = device.asActor().copy(onBehalfOf = user.asMember())

        val error = ErrorFixture.arbitrary()

        whenever(
            authenticationService.augmentDeviceActorOnBehalfOfUser(
                eq(device.asActor()),
                eq(cardIdentity),
                eq(phoneNrIdentity),
                any()
            )
        ).thenReturn(deviceActorOnBehalfOfUser.right())

        whenever(loggingUnlockedTool.logUnlockedTool(eq(deviceActorOnBehalfOfUser), eq(toolId), any()))
            .thenReturn(error.left())

        // when
        val result = notificationHandler.handle(device.asActor(), notification)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }
}