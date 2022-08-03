package cloud.fabX.fabXaccess.user.rest

import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.rest.addBasicAuth
import cloud.fabX.fabXaccess.common.rest.isError
import cloud.fabX.fabXaccess.common.rest.withTestApp
import cloud.fabX.fabXaccess.user.application.AddingUser
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.UserPasswordCredential
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
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

@MockitoSettings
internal class UserControllerAddTest {
    private lateinit var addingUser: AddingUser
    private lateinit var authenticationService: AuthenticationService

    private val username = "some.one"
    private val password = "supersecret123"

    private val actingUser = UserFixture.arbitrary(isAdmin = true)

    @BeforeEach
    fun `configure WebModule`(
        @Mock addingUser: AddingUser,
        @Mock authenticationService: AuthenticationService
    ) {
        this.addingUser = addingUser
        this.authenticationService = authenticationService
    }

    private fun withConfiguredTestApp(block: suspend TestApplicationEngine.() -> Unit) = withTestApp({
        bindInstance(overrides = true) { addingUser }
        bindInstance(overrides = true) { authenticationService }
    }, block)

    @Test
    fun `when adding user then returns http ok`() = withConfiguredTestApp {
        // given
        val requestBody = UserCreationDetails(
            "first",
            "last",
            "wiki"
        )

        val userId = UserIdFixture.arbitrary()

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        whenever(
            addingUser.addUser(
                eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                any(),
                eq("first"),
                eq("last"),
                eq("wiki")
            )
        ).thenReturn(userId.right())

        // when
        val result = handleRequest(HttpMethod.Post, "/api/v1/user") {
            addBasicAuth(username, password)
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(requestBody))
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.OK)
        assertThat(result.response.content).isEqualTo(userId.serialize())
    }

    @Test
    fun `given no admin authentication when adding user then returns http forbidden`() = withConfiguredTestApp {
        // given
        val requestBody = UserCreationDetails(
            "first",
            "last",
            "wiki"
        )

        val message = "msg123"
        val error = Error.UserNotAdmin(message)

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(ErrorPrincipal(error))

        // when
        val result = handleRequest(HttpMethod.Post, "/api/v1/user") {
            addBasicAuth(username, password)
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(requestBody))
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.Forbidden)
        assertThat(result.response.content)
            .isError(
                "UserNotAdmin",
                message
            )
    }

    @Test
    fun `given no body when adding user then returns http unprocessable entity`() = withConfiguredTestApp {
        // given
        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        // when
        val result = handleRequest(HttpMethod.Post, "/api/v1/user") {
            addBasicAuth(username, password)
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            // empty body
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.UnprocessableEntity)
    }

    @Test
    fun `given domain error when adding user then returns mapped domain error`() = withConfiguredTestApp {
        val requestBody = UserCreationDetails(
            "first",
            "last",
            "wiki"
        )

        val correlationId = CorrelationIdFixture.arbitrary()
        val error = Error.WikiNameAlreadyInUse("message 123", correlationId)

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        whenever(
            addingUser.addUser(
                eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                any(),
                eq("first"),
                eq("last"),
                eq("wiki")
            )
        ).thenReturn(error.left())

        // when
        val result = handleRequest(HttpMethod.Post, "/api/v1/user") {
            addBasicAuth(username, password)
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(requestBody))
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.UnprocessableEntity)
        assertThat(result.response.content)
            .isError(
                "WikiNameAlreadyInUse",
                "message 123",
                correlationId = correlationId.serialize()
            )
    }
}