package cloud.fabX.fabXaccess.user.rest

import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.rest.c
import cloud.fabX.fabXaccess.common.rest.isError
import cloud.fabX.fabXaccess.common.rest.withTestApp
import cloud.fabX.fabXaccess.user.application.ChangingPassword
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import cloud.fabX.fabXaccess.user.model.UsernamePasswordIdentity
import io.ktor.client.call.body
import io.ktor.client.request.basicAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.auth.UserPasswordCredential
import io.ktor.server.testing.ApplicationTestBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.kodein.di.bindInstance
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever

@MockitoSettings
internal class UserControllerChangePasswordTest {
    private lateinit var changingPassword: ChangingPassword
    private lateinit var authenticationService: AuthenticationService

    private val username = "some.one"
    private val password = "supersecret123"

    private val actingUser = UserFixture.arbitrary(
        identities = setOf(
            UsernamePasswordIdentity(username, hash(password))
        )
    )

    @BeforeEach
    fun `configure WebModule`(
        @Mock changingPassword: ChangingPassword,
        @Mock authenticationService: AuthenticationService
    ) {
        this.changingPassword = changingPassword
        this.authenticationService = authenticationService
    }

    private fun withConfiguredTestApp(block: suspend ApplicationTestBuilder.() -> Unit) = withTestApp({
        bindInstance(overrides = true) { changingPassword }
        bindInstance(overrides = true) { authenticationService }
    }, block)

    @Test
    fun `when changing password then returns no http content`() = withConfiguredTestApp {
        // given
        val userId = UserIdFixture.arbitrary()

        val newPassword = "newPassword123"
        val requestBody = PasswordChangeDetails(
            newPassword
        )

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        whenever(
            changingPassword.changeOwnPassword(
                eq(actingUser.asMember()),
                any(),
                eq(userId),
                eq(hash(newPassword))
            )
        ).thenReturn(Unit.right())

        // when
        val response = c().post("/api/v1/user/${userId.serialize()}/identity/username-password/change-password") {
            basicAuth(username, password)
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NoContent)
        assertThat(response.bodyAsText()).isEmpty()
    }

    @Test
    fun `given no member authentication when changing password then returns http unauthorized`() = withConfiguredTestApp {
        // given
        val userId = UserIdFixture.arbitrary()

        // when
        val response = c().post("/api/v1/user/${userId.serialize()}/identity/username-password/change-password") {
            // no authentication
            contentType(ContentType.Application.Json)
            setBody(PasswordChangeDetails("newPassword"))
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
        assertThat(response.bodyAsText()).isEmpty()
    }

    @Test
    fun `given no body when changing password then returns http bad request`() = withConfiguredTestApp {
        // given
        val userId = UserIdFixture.arbitrary()

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        // when
        val response = c().post("/api/v1/user/${userId.serialize()}/identity/username-password/change-password") {
            basicAuth(username, password)
            contentType(ContentType.Application.Json)
            // empty body
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.BadRequest)
    }

    @Test
    fun `given invalid user id when changing password then returns http bad request`() = withConfiguredTestApp {
        // given
        val invalidUserId = "invalidUserId"

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        // when
        val response = c().post("/api/v1/user/$invalidUserId/identity/username-password/change-password") {
            basicAuth(username, password)
            contentType(ContentType.Application.Json)
            setBody(PasswordChangeDetails("newPassword"))
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.BadRequest)
        assertThat(response.body<String>())
            .isEqualTo("Required UUID parameter \"id\" not given or invalid.")
    }

    @Test
    fun `given domain error when changing password then returns mapped error`() = withConfiguredTestApp {
        // given
        val userId = UserIdFixture.arbitrary()

        val newPassword = "newPassword123"
        val requestBody = PasswordChangeDetails(
            newPassword
        )

        val correlationId = CorrelationIdFixture.arbitrary()
        val error = Error.UsernamePasswordIdentityNotFound("some message", correlationId)

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        whenever(
            changingPassword.changeOwnPassword(
                eq(actingUser.asMember()),
                any(),
                eq(userId),
                eq(hash(newPassword))
            )
        ).thenReturn(error.left())

        // when
        val response = c().post("/api/v1/user/${userId.serialize()}/identity/username-password/change-password") {
            basicAuth(username, password)
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.UnprocessableEntity)
        assertThat(response.body<cloud.fabX.fabXaccess.common.rest.Error>())
            .isError(
                "UsernamePasswordIdentityNotFound",
                "some message",
                correlationId = correlationId.serialize()
            )

    }
}