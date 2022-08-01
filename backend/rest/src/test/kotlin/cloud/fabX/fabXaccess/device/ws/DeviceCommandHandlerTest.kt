package cloud.fabX.fabXaccess.device.ws

import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.device.application.GettingConfiguration
import cloud.fabX.fabXaccess.device.model.DeviceFixture
import cloud.fabX.fabXaccess.tool.model.IdleState
import cloud.fabX.fabXaccess.tool.model.ToolFixture
import cloud.fabX.fabXaccess.tool.model.ToolType
import isLeft
import isRight
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@MockitoSettings
internal class DeviceCommandHandlerTest {
    private lateinit var commandHandler: DeviceCommandHandler
    private lateinit var gettingConfiguration: GettingConfiguration

    @BeforeEach
    fun `configure RestModule`(
        @Mock gettingConfiguration: GettingConfiguration
    ) {
        this.gettingConfiguration = gettingConfiguration
        this.commandHandler = DeviceCommandHandlerImpl(gettingConfiguration)
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
}