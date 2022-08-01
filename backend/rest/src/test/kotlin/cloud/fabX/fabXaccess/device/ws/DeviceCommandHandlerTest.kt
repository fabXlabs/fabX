package cloud.fabX.fabXaccess.device.ws

import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.ErrorFixture
import cloud.fabX.fabXaccess.device.application.GettingConfiguration
import cloud.fabX.fabXaccess.device.model.DeviceFixture
import cloud.fabX.fabXaccess.tool.model.IdleState
import cloud.fabX.fabXaccess.tool.model.ToolFixture
import cloud.fabX.fabXaccess.tool.model.ToolIdFixture
import cloud.fabX.fabXaccess.tool.model.ToolType
import cloud.fabX.fabXaccess.user.application.GettingAuthorizedTools
import cloud.fabX.fabXaccess.user.model.GettingUserByIdentity
import cloud.fabX.fabXaccess.user.model.UserFixture
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
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@MockitoSettings
internal class DeviceCommandHandlerTest {
    private lateinit var commandHandler: DeviceCommandHandler
    private lateinit var gettingConfiguration: GettingConfiguration
    private lateinit var gettingUserByIdentity: GettingUserByIdentity
    private lateinit var gettingAuthorizedTools: GettingAuthorizedTools

    @BeforeEach
    fun `configure RestModule`(
        @Mock gettingConfiguration: GettingConfiguration,
        @Mock gettingUserByIdentity: GettingUserByIdentity,
        @Mock gettingAuthorizedTools: GettingAuthorizedTools
    ) {
        this.gettingConfiguration = gettingConfiguration
        this.gettingUserByIdentity = gettingUserByIdentity
        this.gettingAuthorizedTools = gettingAuthorizedTools
        this.commandHandler = DeviceCommandHandlerImpl(
            gettingConfiguration,
            gettingUserByIdentity,
            gettingAuthorizedTools
        )
    }

    @Test
    fun `when handling GetConfiguration then returns ConfigurationResponse`() = runTest {
        // given
        val commandId = 123L
        val command = GetConfiguration(commandId)

        val name = "some device"
        val background = "https://example.com/bg123.bmp"
        val backupUrl = "https://api.example.com"
        val mac = "AA11BB22CC33"
        val secret = "abcdef0123456789abcdef0123456789"

        val device = DeviceFixture.arbitrary(
            name = name,
            background = background,
            backupBackendUrl = backupUrl,
            mac = mac,
            secret = secret
        )

        val tool1Name = "tool 1"
        val tool1Time = 1234
        val tool1 = ToolFixture.arbitrary(
            name = tool1Name,
            type = ToolType.UNLOCK,
            time = tool1Time,
            idleState = IdleState.IDLE_LOW
        )

        val tool2Name = "tool 2"
        val tool2Time = 3245
        val tool2 = ToolFixture.arbitrary(
            name = tool2Name,
            type = ToolType.KEEP,
            time = tool2Time,
            idleState = IdleState.IDLE_HIGH
        )

        val actor = device.asActor()

        val configurationResult = GettingConfiguration.Result(device, mapOf(1 to tool1, 2 to tool2))

        val expectedResult = ConfigurationResponse(
            commandId,
            name,
            background,
            backupUrl,
            mapOf(
                1 to ToolConfigurationResponse(
                    tool1Name,
                    cloud.fabX.fabXaccess.tool.rest.ToolType.UNLOCK,
                    tool1Time,
                    cloud.fabX.fabXaccess.tool.rest.IdleState.IDLE_LOW
                ),
                2 to ToolConfigurationResponse(
                    tool2Name,
                    cloud.fabX.fabXaccess.tool.rest.ToolType.KEEP,
                    tool2Time,
                    cloud.fabX.fabXaccess.tool.rest.IdleState.IDLE_HIGH
                )
            )
        )

        whenever(gettingConfiguration.getConfiguration(eq(actor)))
            .thenReturn(configurationResult.right())

        // when
        val result = commandHandler.handle(actor, command)

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(expectedResult)
    }

    @Test
    fun `given error when handling GetConfiguration then returns error`() = runTest {
        // given
        val commandId = 123L
        val command = GetConfiguration(commandId)

        val device = DeviceFixture.arbitrary()
        val actor = device.asActor()

        val error = Error.DeviceNotFound("msg", device.id)

        whenever(gettingConfiguration.getConfiguration(eq(actor)))
            .thenReturn(error.left())

        // when
        val result = commandHandler.handle(actor, command)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }

    @Test
    fun `given card identity when handling GetAuthorizedTools then gets tools on behalf of user`() = runTest {
        // given
        val user = UserFixture.arbitrary()

        val device = DeviceFixture.arbitrary()
        val actor = device.asActor()
        val actorOnBehalfOfUser = actor.copy(onBehalfOf = user.asMember())

        val commandId = 768L

        val cardId = "11223344556677"
        val cardSecret = "EE334F5E740985180C9EDAA6B5A9EB159CFB4F19427C68336D6D23D5015547CE"
        val cardIdentity = CardIdentity(cardId, cardSecret)

        val command = GetAuthorizedTools(commandId, null, cardIdentity)

        val authorizedTool1Id = ToolIdFixture.arbitrary()
        val authorizedTool1 = ToolFixture.arbitrary(authorizedTool1Id)
        val authorizedTool2Id = ToolIdFixture.arbitrary()
        val authorizedTool2 = ToolFixture.arbitrary(authorizedTool2Id)
        val authorizedTools = setOf(authorizedTool1, authorizedTool2)

        whenever(gettingUserByIdentity.getByIdentity(cloud.fabX.fabXaccess.user.model.CardIdentity(cardId, cardSecret)))
            .thenReturn(user.right())

        whenever(gettingAuthorizedTools.getAuthorizedTools(eq(actorOnBehalfOfUser), any()))
            .thenReturn(authorizedTools.right())

        // when
        val result = commandHandler.handle(actor, command)

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(
                AuthorizedToolsResponse(
                    commandId,
                    setOf(authorizedTool1Id.serialize(), authorizedTool2Id.serialize())
                )
            )
    }

    @Test
    fun `given phone nr identity when handling GetAuthorizedTools then gets tools on behalf of user`() = runTest {
        // given
        val user = UserFixture.arbitrary()

        val device = DeviceFixture.arbitrary()
        val actor = device.asActor()
        val actorOnBehalfOfUser = actor.copy(onBehalfOf = user.asMember())

        val commandId = 768L

        val phoneNr = "+49123456789"
        val phoneNrIdentity = PhoneNrIdentity(phoneNr)

        val command = GetAuthorizedTools(commandId, phoneNrIdentity, null)

        val authorizedTool1Id = ToolIdFixture.arbitrary()
        val authorizedTool1 = ToolFixture.arbitrary(authorizedTool1Id)
        val authorizedTools = setOf(authorizedTool1)

        whenever(gettingUserByIdentity.getByIdentity(cloud.fabX.fabXaccess.user.model.PhoneNrIdentity(phoneNr)))
            .thenReturn(user.right())

        whenever(gettingAuthorizedTools.getAuthorizedTools(eq(actorOnBehalfOfUser), any()))
            .thenReturn(authorizedTools.right())

        // when
        val result = commandHandler.handle(actor, command)

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(
                AuthorizedToolsResponse(
                    commandId,
                    setOf(authorizedTool1Id.serialize())
                )
            )
    }

    @Test
    fun `given matching card and phone nr identity when handling GetAuthorizedTools then gets tools on behalf of user`() =
        runTest {
            // given
            val user = UserFixture.arbitrary()

            val device = DeviceFixture.arbitrary()
            val actor = device.asActor()
            val actorOnBehalfOfUser = actor.copy(onBehalfOf = user.asMember())

            val commandId = 768L

            val phoneNr = "+49123456789"
            val phoneNrIdentity = PhoneNrIdentity(phoneNr)

            val cardId = "11223344556677"
            val cardSecret = "EE334F5E740985180C9EDAA6B5A9EB159CFB4F19427C68336D6D23D5015547CE"
            val cardIdentity = CardIdentity(cardId, cardSecret)

            val command = GetAuthorizedTools(commandId, phoneNrIdentity, cardIdentity)

            val authorizedTool1Id = ToolIdFixture.arbitrary()
            val authorizedTool1 = ToolFixture.arbitrary(authorizedTool1Id)
            val authorizedTool2Id = ToolIdFixture.arbitrary()
            val authorizedTool2 = ToolFixture.arbitrary(authorizedTool2Id)
            val authorizedTools = setOf(authorizedTool1, authorizedTool2)

            whenever(gettingUserByIdentity.getByIdentity(cloud.fabX.fabXaccess.user.model.PhoneNrIdentity(phoneNr)))
                .thenReturn(user.right())

            whenever(
                gettingUserByIdentity.getByIdentity(
                    cloud.fabX.fabXaccess.user.model.CardIdentity(
                        cardId,
                        cardSecret
                    )
                )
            )
                .thenReturn(user.right())

            whenever(gettingAuthorizedTools.getAuthorizedTools(eq(actorOnBehalfOfUser), any()))
                .thenReturn(authorizedTools.right())

            // when
            val result = commandHandler.handle(actor, command)

            // then
            assertThat(result)
                .isRight()
                .isEqualTo(
                    AuthorizedToolsResponse(
                        commandId,
                        setOf(authorizedTool1Id.serialize(), authorizedTool2Id.serialize())
                    )
                )
        }

    @Test
    fun `given non-matching card and phone nr identity when handling GetAuthorizedTools then returns error`() =
        runTest {
            // given
            val user = UserFixture.arbitrary()
            val otherUser = UserFixture.arbitrary()
            assertThat(user.id).isNotEqualTo(otherUser.id)

            val actor = DeviceFixture.arbitrary().asActor()

            val commandId = 768L

            val phoneNr = "+49123456789"
            val phoneNrIdentity = PhoneNrIdentity(phoneNr)

            val cardId = "11223344556677"
            val cardSecret = "EE334F5E740985180C9EDAA6B5A9EB159CFB4F19427C68336D6D23D5015547CE"
            val cardIdentity = CardIdentity(cardId, cardSecret)

            val command = GetAuthorizedTools(commandId, phoneNrIdentity, cardIdentity)

            whenever(gettingUserByIdentity.getByIdentity(cloud.fabX.fabXaccess.user.model.PhoneNrIdentity(phoneNr)))
                .thenReturn(user.right())

            whenever(
                gettingUserByIdentity.getByIdentity(
                    cloud.fabX.fabXaccess.user.model.CardIdentity(
                        cardId,
                        cardSecret
                    )
                )
            )
                .thenReturn(otherUser.right())

            // when
            val result = commandHandler.handle(actor, command)

            // then
            assertThat(result)
                .isLeft()
                .isEqualTo(Error.NotAuthenticated("Required authentication not found."))
        }

    @Test
    fun `given no user identity when handling GetAuthorizedTools then does not act on behalf of user`() = runTest {
        // given
        val commandId = 876L
        val command = GetAuthorizedTools(commandId, null, null)

        val actor = DeviceFixture.arbitrary().asActor()

        val error = ErrorFixture.arbitrary()

        whenever(gettingAuthorizedTools.getAuthorizedTools(eq(actor), any()))
            .thenReturn(error.left())

        // when
        val result = commandHandler.handle(actor, command)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }

    @Test
    fun `given invalid card identity when handling GetAuthorizedTools then returns error`() = runTest {
        // given
        val actor = DeviceFixture.arbitrary().asActor()

        val commandId = 768L

        val cardId = "11223344556677"
        val cardSecret = "EE334F5E740985180C9EDAA6B5A9EB159CFB4F19427C68336D6D23D5015547CE"
        val cardIdentity = CardIdentity(cardId, cardSecret)

        val command = GetAuthorizedTools(commandId, null, cardIdentity)

        val error = ErrorFixture.arbitrary()

        whenever(gettingUserByIdentity.getByIdentity(cloud.fabX.fabXaccess.user.model.CardIdentity(cardId, cardSecret)))
            .thenReturn(error.left())

        // when
        val result = commandHandler.handle(actor, command)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }

    @Test
    fun `given invalid phone nr identity when handling GetAuthorizedTools then returns error`() = runTest {
        // given
        val actor = DeviceFixture.arbitrary().asActor()

        val commandId = 768L

        val phoneNr = "+49123456789"
        val phoneNrIdentity = PhoneNrIdentity(phoneNr)


        val command = GetAuthorizedTools(commandId, phoneNrIdentity, null)

        val error = ErrorFixture.arbitrary()

        whenever(gettingUserByIdentity.getByIdentity(cloud.fabX.fabXaccess.user.model.PhoneNrIdentity(phoneNr)))
            .thenReturn(error.left())

        // when
        val result = commandHandler.handle(actor, command)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }
}