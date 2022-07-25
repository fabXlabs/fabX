package cloud.fabX.fabXaccess.tool.rest

import arrow.core.None
import arrow.core.getOrElse
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.rest.ChangeableValue
import cloud.fabX.fabXaccess.common.rest.addBasicAuth
import cloud.fabX.fabXaccess.common.rest.isJson
import cloud.fabX.fabXaccess.common.rest.withTestApp
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFixture
import cloud.fabX.fabXaccess.tool.application.ChangingTool
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
class ToolControllerPutTest {
    private lateinit var changingTool: ChangingTool
    private lateinit var authenticationService: AuthenticationService

    private val username = "some.one"
    private val password = "supersecret123"

    private val actingUser = UserFixture.arbitrary(isAdmin = true)

    @BeforeEach
    fun `configure RestModule`(
        @Mock changingTool: ChangingTool,
        @Mock authenticationService: AuthenticationService
    ) {
        this.changingTool = changingTool
        this.authenticationService = authenticationService
    }

    private fun withConfiguredTestApp(block: TestApplicationEngine.() -> Unit) = withTestApp({
        bindInstance(overrides = true) { changingTool }
        bindInstance(overrides = true) { authenticationService }
    }, block)

    @Test
    fun `when changing tool then returns http ok`() = withConfiguredTestApp {
        // given
        val id = ToolIdFixture.arbitrary()

        val qualificationId1 = QualificationIdFixture.arbitrary()
        val qualificationId2 = QualificationIdFixture.arbitrary()

        val requestBody = ToolDetails(
            ChangeableValue("newName"),
            ChangeableValue(ToolType.UNLOCK),
            null,
            ChangeableValue(IdleState.IDLE_HIGH),
            ChangeableValue(false),
            null,
            ChangeableValue(setOf(qualificationId1.serialize(), qualificationId2.serialize()))
        )

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        whenever(
            changingTool.changeToolDetails(
                eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                any(),
                eq(id),
                eq(cloud.fabX.fabXaccess.common.model.ChangeableValue.ChangeToValue("newName")),
                eq(cloud.fabX.fabXaccess.common.model.ChangeableValue.ChangeToValue(cloud.fabX.fabXaccess.tool.model.ToolType.UNLOCK)),
                eq(cloud.fabX.fabXaccess.common.model.ChangeableValue.LeaveAsIs),
                eq(cloud.fabX.fabXaccess.common.model.ChangeableValue.ChangeToValue(cloud.fabX.fabXaccess.tool.model.IdleState.IDLE_HIGH)),
                eq(cloud.fabX.fabXaccess.common.model.ChangeableValue.ChangeToValue(false)),
                eq(cloud.fabX.fabXaccess.common.model.ChangeableValue.LeaveAsIs),
                eq(
                    cloud.fabX.fabXaccess.common.model.ChangeableValue.ChangeToValue(
                        setOf(qualificationId1, qualificationId2)
                    )
                )
            )
        ).thenReturn(None)

        // when
        val result = handleRequest(HttpMethod.Put, "/api/v1/tool/${id.serialize()}") {
            addBasicAuth(username, password)
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(requestBody))
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.OK)
        assertThat(result.response.content).isNull()
    }

    @Test
    fun `given no admin authentication when changing tool then returns http forbidden`() = withConfiguredTestApp {
        // given
        val requestBody = ToolDetails(
            null,
            null,
            null,
            null,
            null,
            null,
            null
        )

        val message = "abc1234"
        val error = Error.UserNotAdmin(message)

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(ErrorPrincipal(error))

        // when
        val result = handleRequest(HttpMethod.Put, "/api/v1/tool/${ToolIdFixture.arbitrary().serialize()}") {
            addBasicAuth(username, password)
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(requestBody))
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.Forbidden)
        assertThat(result.response.content)
            .isNotNull()
            .isJson<cloud.fabX.fabXaccess.common.rest.Error>()
            .isEqualTo(
                cloud.fabX.fabXaccess.common.rest.Error(
                    message,
                    mapOf()
                )
            )
    }

    @Test
    fun `given no body when changing tool then returns http unprocessable entity`() = withConfiguredTestApp {
        // given
        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        // when
        val result = handleRequest(HttpMethod.Put, "/api/v1/tool/${ToolIdFixture.arbitrary().serialize()}") {
            addBasicAuth(username, password)
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            // empty body
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.UnprocessableEntity)
    }
}