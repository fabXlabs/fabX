package cloud.fabX.fabXaccess.tool.rest

import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.rest.addBasicAuth
import cloud.fabX.fabXaccess.common.rest.isError
import cloud.fabX.fabXaccess.common.rest.isJson
import cloud.fabX.fabXaccess.common.rest.withTestApp
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFixture
import cloud.fabX.fabXaccess.tool.application.GettingTool
import cloud.fabX.fabXaccess.tool.model.IdleState
import cloud.fabX.fabXaccess.tool.model.ToolFixture
import cloud.fabX.fabXaccess.tool.model.ToolIdFixture
import cloud.fabX.fabXaccess.tool.model.ToolType
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.rest.AuthenticationService
import cloud.fabX.fabXaccess.user.rest.UserPrincipal
import io.ktor.auth.UserPasswordCredential
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.util.InternalAPI
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.kodein.di.bindInstance
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever

@InternalAPI
@ExperimentalSerializationApi
@MockitoSettings
internal class ToolControllerGetTest {
    private lateinit var gettingTool: GettingTool
    private lateinit var authenticationService: AuthenticationService

    private val username = "some.one"
    private val password = "supersecret123"

    private val actingUser = UserFixture.arbitrary()

    @BeforeEach
    fun `configure RestModule`(
        @Mock gettingTool: GettingTool,
        @Mock authenticationService: AuthenticationService
    ) {
        this.gettingTool = gettingTool
        this.authenticationService = authenticationService

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))
    }

    private fun withConfiguredTestApp(block: suspend TestApplicationEngine.() -> Unit) = withTestApp({
        bindInstance(overrides = true) { gettingTool }
        bindInstance(overrides = true) { authenticationService }
    }, block)

    @Test
    fun `given no tools when get tools then returns empty set`() = withConfiguredTestApp {
        // given
        whenever(gettingTool.getAll(eq(actingUser.asMember()), any()))
            .thenReturn(setOf())

        // when
        val result = handleRequest(HttpMethod.Get, "/api/v1/tool") {
            addBasicAuth(username, password)
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.OK)
        assertThat(result.response.content)
            .isNotNull()
            .isJson<Set<Tool>>()
            .isEqualTo(setOf())
    }

    @Test
    fun `when get tools then returns mapped tools`() = withConfiguredTestApp {
        // given
        val qualificationId1 = QualificationIdFixture.arbitrary()
        val qualificationId2 = QualificationIdFixture.arbitrary()

        val toolId1 = ToolIdFixture.arbitrary()
        val tool1 = ToolFixture.arbitrary(
            toolId1,
            123,
            "tool1",
            ToolType.KEEP,
            567,
            IdleState.IDLE_LOW,
            true,
            "https://example.com/tool1",
            setOf(qualificationId1, qualificationId2)
        )

        val mappedTool1 = Tool(
            toolId1.serialize(),
            123,
            "tool1",
            cloud.fabX.fabXaccess.tool.rest.ToolType.KEEP,
            567,
            cloud.fabX.fabXaccess.tool.rest.IdleState.IDLE_LOW,
            true,
            "https://example.com/tool1",
            setOf(qualificationId1.serialize(), qualificationId2.serialize())
        )

        val toolId2 = ToolIdFixture.arbitrary()
        val tool2 = ToolFixture.arbitrary(
            toolId2,
            234,
            "tool2",
            ToolType.UNLOCK,
            890,
            IdleState.IDLE_HIGH,
            false,
            "https://example.com/tool2",
            setOf(qualificationId2)
        )

        val mappedTool2 = Tool(
            toolId2.serialize(),
            234,
            "tool2",
            cloud.fabX.fabXaccess.tool.rest.ToolType.UNLOCK,
            890,
            cloud.fabX.fabXaccess.tool.rest.IdleState.IDLE_HIGH,
            false,
            "https://example.com/tool2",
            setOf(qualificationId2.serialize())
        )

        whenever(gettingTool.getAll(eq(actingUser.asMember()), any()))
            .thenReturn(setOf(tool1, tool2))

        // when
        val result = handleRequest(HttpMethod.Get, "/api/v1/tool") {
            addBasicAuth(username, password)
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.OK)
        assertThat(result.response.content)
            .isNotNull()
            .isJson<Set<Tool>>()
            .isEqualTo(setOf(mappedTool1, mappedTool2))
    }

    @Test
    fun `given tool exists when get tool by id then returns mapped tool`() = withConfiguredTestApp {
        // given
        val qualificationId1 = QualificationIdFixture.arbitrary()
        val qualificationId2 = QualificationIdFixture.arbitrary()

        val toolId = ToolIdFixture.arbitrary()
        val tool = ToolFixture.arbitrary(
            toolId,
            123,
            "tool",
            ToolType.KEEP,
            567,
            IdleState.IDLE_LOW,
            true,
            "https://example.com/tool",
            setOf(qualificationId1, qualificationId2)
        )

        val mappedTool = Tool(
            toolId.serialize(),
            123,
            "tool",
            cloud.fabX.fabXaccess.tool.rest.ToolType.KEEP,
            567,
            cloud.fabX.fabXaccess.tool.rest.IdleState.IDLE_LOW,
            true,
            "https://example.com/tool",
            setOf(qualificationId1.serialize(), qualificationId2.serialize())
        )

        whenever(gettingTool.getById(eq(actingUser.asMember()), any(), eq(toolId)))
            .thenReturn(tool.right())

        // when
        val result = handleRequest(HttpMethod.Get, "/api/v1/tool/${toolId.serialize()}") {
            addBasicAuth(username, password)
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.OK)
        assertThat(result.response.content)
            .isNotNull()
            .isJson<Tool>()
            .isEqualTo(mappedTool)
    }

    @Test
    fun `given tool not found when get tool by id then returns mapped error`() = withConfiguredTestApp {
        // given
        val toolId = ToolIdFixture.arbitrary()
        val error = Error.ToolNotFound("msg", toolId)

        whenever(gettingTool.getById(eq(actingUser.asMember()), any(), eq(toolId)))
            .thenReturn(error.left())

        // when
        val result = handleRequest(HttpMethod.Get, "/api/v1/tool/${toolId.serialize()}") {
            addBasicAuth(username, password)
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.NotFound)
        assertThat(result.response.content)
            .isError(
                "ToolNotFound",
                "msg",
                mapOf("toolId" to toolId.serialize())
            )
    }
}