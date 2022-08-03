package cloud.fabX.fabXaccess.user.rest

import arrow.core.None
import arrow.core.getOrElse
import arrow.core.some
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.rest.addBasicAuth
import cloud.fabX.fabXaccess.common.rest.isError
import cloud.fabX.fabXaccess.common.rest.withTestApp
import cloud.fabX.fabXaccess.user.application.ChangingIsAdmin
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
internal class UserControllerChangeIsAdminTest {
    private lateinit var changingIsAdmin: ChangingIsAdmin
    private lateinit var authenticationService: AuthenticationService

    private val username = "some.one"
    private val password = "supersecret123"

    private val actingUser = UserFixture.arbitrary(isAdmin = true)

    @BeforeEach
    fun `configure WebModule`(
        @Mock changingIsAdmin: ChangingIsAdmin,
        @Mock authenticationService: AuthenticationService
    ) {
        this.changingIsAdmin = changingIsAdmin
        this.authenticationService = authenticationService
    }

    private fun withConfiguredTestApp(block: suspend TestApplicationEngine.() -> Unit) = withTestApp({
        bindInstance(overrides = true) { changingIsAdmin }
        bindInstance(overrides = true) { authenticationService }
    }, block)

    @Test
    fun `when changing is admin then returns http no content`() = withConfiguredTestApp {
        // given
        val id = UserIdFixture.arbitrary()

        val requestBody = IsAdminDetails(
            true
        )

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        whenever(
            changingIsAdmin.changeIsAdmin(
                eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                any(),
                eq(id),
                eq(true)
            )
        ).thenReturn(None)

        // when
        val result = handleRequest(HttpMethod.Put, "/api/v1/user/${id.serialize()}/is-admin") {
            addBasicAuth(username, password)
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(requestBody))
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.NoContent)
        assertThat(result.response.content).isNull()
    }

    @Test
    fun `given no admin authentication when changing is admin then returns http forbidden`() = withConfiguredTestApp {
        // given
        val requestBody = IsAdminDetails(
            true
        )

        val message = "error message"
        val error = Error.UserNotAdmin(message)

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(ErrorPrincipal(error))

        // when
        val result = handleRequest(HttpMethod.Put, "/api/v1/user/${UserIdFixture.arbitrary().serialize()}/is-admin") {
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
    fun `given no body when changing is admin then returns http unprocessable entity`() = withConfiguredTestApp {
        // given
        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        // when
        val result = handleRequest(HttpMethod.Put, "/api/v1/user/${UserIdFixture.arbitrary().serialize()}/is-admin") {
            addBasicAuth(username, password)
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            // empty body
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.UnprocessableEntity)
    }

    @Test
    fun `given invalid user id when changing is admin then returns http bad request`() = withConfiguredTestApp {
        // given
        val invalidUserId = "invalidUserId"

        val requestBody = IsAdminDetails(
            true
        )

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))


        // when
        val result = handleRequest(HttpMethod.Put, "/api/v1/user/$invalidUserId/is-admin") {
            addBasicAuth(username, password)
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(requestBody))
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.BadRequest)
        assertThat(result.response.content)
            .isNotNull()
            .isEqualTo("Required UUID parameter \"id\" not given or invalid.")
    }

    @Test
    fun `given domain error when changing is admin then returns mapped domain error`() = withConfiguredTestApp {
        // given
        val id = UserIdFixture.arbitrary()

        val requestBody = IsAdminDetails(
            true
        )

        val correlationId = CorrelationIdFixture.arbitrary()
        val error = Error.UserAlreadyAdmin("msg123", correlationId)

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        whenever(
            changingIsAdmin.changeIsAdmin(
                eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                any(),
                eq(id),
                eq(true)
            )
        ).thenReturn(error.some())

        // when
        val result = handleRequest(HttpMethod.Put, "/api/v1/user/${id.serialize()}/is-admin") {
            addBasicAuth(username, password)
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(requestBody))
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.UnprocessableEntity)
        assertThat(result.response.content)
            .isError(
                "UserAlreadyAdmin",
                "msg123",
                correlationId = correlationId.serialize()
            )
    }
}