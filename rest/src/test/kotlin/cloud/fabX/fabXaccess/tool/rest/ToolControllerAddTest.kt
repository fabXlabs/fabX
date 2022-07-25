package cloud.fabX.fabXaccess.tool.rest

import arrow.core.getOrElse
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.rest.addBasicAuth
import cloud.fabX.fabXaccess.common.rest.isJson
import cloud.fabX.fabXaccess.common.rest.withTestApp
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFixture
import cloud.fabX.fabXaccess.tool.application.AddingTool
import cloud.fabX.fabXaccess.tool.model.ToolIdFixture
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.rest.AuthenticationService
import cloud.fabX.fabXaccess.user.rest.ErrorPrincipal
import cloud.fabX.fabXaccess.user.rest.UserPrincipal
import io.ktor.auth.UserPasswordCredential
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.util.InternalAPI
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
class ToolControllerAddTest {
    private lateinit var addingTool: AddingTool
    private lateinit var authenticationService: AuthenticationService

    private val username = "some.one"
    private val password = "supersecret123"

    private val actingUser = UserFixture.arbitrary(isAdmin = true)

    @BeforeEach
    fun `configure RestModule`(
        @Mock addingTool: AddingTool,
        @Mock authenticationService: AuthenticationService
    ) {
        this.addingTool = addingTool
        this.authenticationService = authenticationService
    }

    private fun withConfiguredTestApp(block: TestApplicationEngine.() -> Unit) = withTestApp({
        bindInstance(overrides = true) { addingTool }
        bindInstance(overrides = true) { authenticationService }
    }, block)

    @Test
    fun `when adding tool then returns http ok`() = withConfiguredTestApp {
        // given
        val qualificationId1 = QualificationIdFixture.arbitrary()
        val qualificationId2 = QualificationIdFixture.arbitrary()

        val requestBody = ToolCreationDetails(
            "tool1",
            ToolType.UNLOCK,
            123,
            IdleState.IDLE_LOW,
            "https://example.com/tool1",
            setOf(qualificationId1.serialize(), qualificationId2.serialize())
        )

        val toolId = ToolIdFixture.arbitrary()

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        whenever(
            addingTool.addTool(
                eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                any(),
                eq("tool1"),
                eq(cloud.fabX.fabXaccess.tool.model.ToolType.UNLOCK),
                eq(123),
                eq(cloud.fabX.fabXaccess.tool.model.IdleState.IDLE_LOW),
                eq("https://example.com/tool1"),
                eq(setOf(qualificationId1, qualificationId2))
            )
        ).thenReturn(toolId.right())

        // when
        val result = handleRequest(HttpMethod.Post, "/api/v1/tool") {
            addBasicAuth(username, password)
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(requestBody))
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.OK)
        assertThat(result.response.content).isEqualTo(toolId.serialize())
    }

    @Test
    fun `given no admin authentication when adding tool then returns http forbidden`() = withConfiguredTestApp {
        // given
        val requestBody = ToolCreationDetails(
            "tool1",
            ToolType.UNLOCK,
            123,
            IdleState.IDLE_LOW,
            "https://example.com/tool1",
            setOf()
        )

        val message = "msg"
        val error = Error.UserNotAdmin(message)

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(ErrorPrincipal(error))

        // when
        val result = handleRequest(HttpMethod.Post, "/api/v1/tool") {
            addBasicAuth(username, password)
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(requestBody))
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.Forbidden)
        assertThat(result.response.content)
            .isNotNull()
            .isJson<cloud.fabX.fabXaccess.common.rest.Error>()
            .transform { it.message }
            .isEqualTo(message)
    }
}