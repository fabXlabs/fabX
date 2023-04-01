package cloud.fabX.fabXaccess.device.ws

import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
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
import cloud.fabX.fabXaccess.user.application.ValidatingSecondFactor
import cloud.fabX.fabXaccess.user.model.PinIdentity
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.rest.AuthenticationService
import cloud.fabX.fabXaccess.user.rest.CardIdentity
import cloud.fabX.fabXaccess.user.rest.PhoneNrIdentity
import cloud.fabX.fabXaccess.user.rest.PinIdentityDetails
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
internal class DeviceCommandHandlerTest {
    private lateinit var gettingConfiguration: GettingConfiguration
    private lateinit var gettingAuthorizedTools: GettingAuthorizedTools
    private lateinit var validatingSecondFactor: ValidatingSecondFactor
    private lateinit var authenticationService: AuthenticationService

    private lateinit var commandHandler: DeviceCommandHandler

    @BeforeEach
    fun `configure WebModule`(
        @Mock gettingConfiguration: GettingConfiguration,
        @Mock gettingAuthorizedTools: GettingAuthorizedTools,
        @Mock validatingSecondFactor: ValidatingSecondFactor,
        @Mock authenticationService: AuthenticationService
    ) {
        this.gettingConfiguration = gettingConfiguration
        this.gettingAuthorizedTools = gettingAuthorizedTools
        this.validatingSecondFactor = validatingSecondFactor
        this.authenticationService = authenticationService

        this.commandHandler = DeviceCommandHandlerImpl(
            gettingConfiguration,
            gettingAuthorizedTools,
            validatingSecondFactor,
            authenticationService
        )
    }

    @Test
    fun `when handling GetConfiguration then returns ConfigurationResponse`() = runTest {
        // given
        val commandId = 123
        val command = GetConfiguration(commandId, "42.1.2")

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

        val tool1Id = ToolIdFixture.arbitrary()
        val tool1Name = "tool 1"
        val tool1Time = 1234
        val tool1Requires2FA = false
        val tool1 = ToolFixture.arbitrary(
            toolId = tool1Id,
            name = tool1Name,
            type = ToolType.UNLOCK,
            requires2FA = tool1Requires2FA,
            time = tool1Time,
            idleState = IdleState.IDLE_LOW
        )

        val tool2Id = ToolIdFixture.arbitrary()
        val tool2Name = "tool 2"
        val tool2Time = 3245
        val tool2Requires2FA = true
        val tool2 = ToolFixture.arbitrary(
            toolId = tool2Id,
            name = tool2Name,
            type = ToolType.KEEP,
            requires2FA = tool2Requires2FA,
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
                    tool1Id.serialize(),
                    tool1Name,
                    cloud.fabX.fabXaccess.tool.rest.ToolType.UNLOCK,
                    tool1Requires2FA,
                    tool1Time,
                    cloud.fabX.fabXaccess.tool.rest.IdleState.IDLE_LOW
                ),
                2 to ToolConfigurationResponse(
                    tool2Id.serialize(),
                    tool2Name,
                    cloud.fabX.fabXaccess.tool.rest.ToolType.KEEP,
                    tool2Requires2FA,
                    tool2Time,
                    cloud.fabX.fabXaccess.tool.rest.IdleState.IDLE_HIGH
                )
            )
        )

        whenever(gettingConfiguration.getConfiguration(eq(actor), any(), eq("42.1.2")))
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
        val commandId = 123
        val command = GetConfiguration(commandId, "42.1.2")

        val device = DeviceFixture.arbitrary()
        val actor = device.asActor()

        val error = Error.DeviceNotFound("msg", device.id)

        whenever(gettingConfiguration.getConfiguration(eq(actor), any(), eq("42.1.2")))
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

        val commandId = 768

        val cardId = "11223344556677"
        val cardSecret = "EE334F5E740985180C9EDAA6B5A9EB159CFB4F19427C68336D6D23D5015547CE"
        val cardIdentity = CardIdentity(cardId, cardSecret)

        val command = GetAuthorizedTools(commandId, null, cardIdentity)

        val authorizedTool1Id = ToolIdFixture.arbitrary()
        val authorizedTool1 = ToolFixture.arbitrary(authorizedTool1Id)
        val authorizedTool2Id = ToolIdFixture.arbitrary()
        val authorizedTool2 = ToolFixture.arbitrary(authorizedTool2Id)
        val authorizedTools = setOf(authorizedTool1, authorizedTool2)

        whenever(authenticationService.augmentDeviceActorOnBehalfOfUser(eq(actor), eq(cardIdentity), isNull(), any()))
            .thenReturn(actorOnBehalfOfUser.right())

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

        val commandId = 768

        val phoneNr = "+49123456789"
        val phoneNrIdentity = PhoneNrIdentity(phoneNr)

        val command = GetAuthorizedTools(commandId, phoneNrIdentity, null)

        val authorizedTool1Id = ToolIdFixture.arbitrary()
        val authorizedTool1 = ToolFixture.arbitrary(authorizedTool1Id)
        val authorizedTools = setOf(authorizedTool1)

        whenever(
            authenticationService.augmentDeviceActorOnBehalfOfUser(
                eq(actor),
                isNull(),
                eq(phoneNrIdentity),
                any()
            )
        )
            .thenReturn(actorOnBehalfOfUser.right())

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

            val commandId = 768

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

            whenever(
                authenticationService.augmentDeviceActorOnBehalfOfUser(
                    eq(actor),
                    eq(cardIdentity),
                    eq(phoneNrIdentity),
                    any()
                )
            ).thenReturn(actorOnBehalfOfUser.right())

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
    fun `given error when augmenting device actor on behalf of user when handling GetAuthorizedTools then returns error`() =
        runTest {
            // given
            val user = UserFixture.arbitrary()
            val otherUser = UserFixture.arbitrary()
            assertThat(user.id).isNotEqualTo(otherUser.id)

            val actor = DeviceFixture.arbitrary().asActor()

            val commandId = 768

            val phoneNr = "+49123456789"
            val phoneNrIdentity = PhoneNrIdentity(phoneNr)

            val cardId = "11223344556677"
            val cardSecret = "EE334F5E740985180C9EDAA6B5A9EB159CFB4F19427C68336D6D23D5015547CE"
            val cardIdentity = CardIdentity(cardId, cardSecret)

            val command = GetAuthorizedTools(commandId, phoneNrIdentity, cardIdentity)

            val error = Error.NotAuthenticated("Required authentication not found.")

            whenever(
                authenticationService.augmentDeviceActorOnBehalfOfUser(
                    eq(actor),
                    eq(cardIdentity),
                    eq(phoneNrIdentity),
                    any()
                )
            ).thenReturn(error.left())

            // when
            val result = commandHandler.handle(actor, command)

            // then
            assertThat(result)
                .isLeft()
                .isEqualTo(error)
        }

    @Test
    fun `given no user identity when handling GetAuthorizedTools then does not act on behalf of user`() = runTest {
        // given
        val commandId = 876
        val command = GetAuthorizedTools(commandId, null, null)

        val actor = DeviceFixture.arbitrary().asActor()

        val error = ErrorFixture.arbitrary()

        whenever(
            authenticationService.augmentDeviceActorOnBehalfOfUser(
                eq(actor),
                isNull(),
                isNull(),
                any()
            )
        ).thenReturn(actor.right())

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
    fun `given valid second factor when handling ValidateSecondFactor then returns ValidSecondFactorResponse`() =
        runTest {
            // given
            val user = UserFixture.arbitrary()

            val device = DeviceFixture.arbitrary()
            val actor = device.asActor()
            val actorOnBehalfOfUser = actor.copy(onBehalfOf = user.asMember())

            // primary factor
            val cardId = "11223344556677"
            val cardSecret = "EE334F5E740985180C9EDAA6B5A9EB159CFB4F19427C68336D6D23D5015547CE"
            val cardIdentity = CardIdentity(cardId, cardSecret)

            // second factor
            val pinIdentity = PinIdentityDetails("7890")

            val commandId = 865

            val command = ValidateSecondFactor(commandId, null, cardIdentity, pinIdentity)

            whenever(
                authenticationService.augmentDeviceActorOnBehalfOfUser(
                    eq(actor),
                    eq(cardIdentity),
                    isNull(),
                    any()
                )
            )
                .thenReturn(actorOnBehalfOfUser.right())

            whenever(
                validatingSecondFactor.validateSecondFactor(
                    eq(actorOnBehalfOfUser),
                    any(),
                    eq(PinIdentity("7890"))
                )
            )
                .thenReturn(Unit.right())

            // when
            val result = commandHandler.handle(actor, command)

            // then
            assertThat(result)
                .isRight()
                .isEqualTo(ValidSecondFactorResponse(commandId))
        }

    @Test
    fun `given invalid second factor when handling ValidateSecondFactor then returns ErrorResponse`() = runTest {
        // given
        val user = UserFixture.arbitrary()

        val device = DeviceFixture.arbitrary()
        val actor = device.asActor()
        val actorOnBehalfOfUser = actor.copy(onBehalfOf = user.asMember())

        // primary factor
        val cardId = "11223344556677"
        val cardSecret = "EE334F5E740985180C9EDAA6B5A9EB159CFB4F19427C68336D6D23D5015547CE"
        val cardIdentity = CardIdentity(cardId, cardSecret)

        // second factor
        val pinIdentity = PinIdentityDetails("7890")

        val commandId = 865

        val command = ValidateSecondFactor(commandId, null, cardIdentity, pinIdentity)

        val error = ErrorFixture.arbitrary()

        whenever(
            authenticationService.augmentDeviceActorOnBehalfOfUser(
                eq(actor),
                eq(cardIdentity),
                isNull(),
                any()
            )
        )
            .thenReturn(actorOnBehalfOfUser.right())

        whenever(
            validatingSecondFactor.validateSecondFactor(
                eq(actorOnBehalfOfUser),
                any(),
                eq(PinIdentity("7890"))
            )
        )
            .thenReturn(error.left())

        // when
        val result = commandHandler.handle(actor, command)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }

    @Test
    fun `given no second factor when handling ValidateSecondFactor then returns InvalidSecondFactor`() = runTest {
        // given
        val user = UserFixture.arbitrary()

        val device = DeviceFixture.arbitrary()
        val actor = device.asActor()
        val actorOnBehalfOfUser = actor.copy(onBehalfOf = user.asMember())

        // primary factor
        val cardId = "11223344556677"
        val cardSecret = "EE334F5E740985180C9EDAA6B5A9EB159CFB4F19427C68336D6D23D5015547CE"
        val cardIdentity = CardIdentity(cardId, cardSecret)

        val commandId = 865

        val command = ValidateSecondFactor(commandId, null, cardIdentity, pinSecondIdentity = null)

        whenever(
            authenticationService.augmentDeviceActorOnBehalfOfUser(
                eq(actor),
                eq(cardIdentity),
                isNull(),
                any()
            )
        )
            .thenReturn(actorOnBehalfOfUser.right())

        // when
        val result = commandHandler.handle(actor, command)

        // then
        assertThat(result)
            .isLeft()
            .isInstanceOf(Error.InvalidSecondFactor::class)
            .transform { it.message }
            .isEqualTo("Second Factor not provided.")
    }

    @Test
    fun `given invalid primary factor when handling ValidateSecondFactor then returns Error`() = runTest {
        // given
        val device = DeviceFixture.arbitrary()
        val actor = device.asActor()

        // primary factor
        val cardId = "11223344556677"
        val cardSecret = "EE334F5E740985180C9EDAA6B5A9EB159CFB4F19427C68336D6D23D5015547CE"
        val cardIdentity = CardIdentity(cardId, cardSecret)

        val commandId = 865

        val command = ValidateSecondFactor(commandId, null, cardIdentity, pinSecondIdentity = null)

        val error = ErrorFixture.arbitrary()

        whenever(
            authenticationService.augmentDeviceActorOnBehalfOfUser(
                eq(actor),
                eq(cardIdentity),
                isNull(),
                any()
            )
        )
            .thenReturn(error.left())

        // when
        val result = commandHandler.handle(actor, command)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }
}