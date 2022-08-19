package cloud.fabX.fabXaccess.user.rest

import arrow.core.None
import arrow.core.some
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.rest.c
import cloud.fabX.fabXaccess.common.rest.isError
import cloud.fabX.fabXaccess.common.rest.withTestApp
import cloud.fabX.fabXaccess.user.application.AddingWebauthnIdentity
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.model.UserIdFixture
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
internal class UserControllerAddWebauthnIdentityTest {
    private lateinit var addingWebauthnIdentity: AddingWebauthnIdentity
    private lateinit var authenticationService: AuthenticationService

    private val username = "some.one"
    private val password = "supersecret123"

    private val actingUser = UserFixture.arbitrary()

    @BeforeEach
    fun `configure WebModule`(
        @Mock addingWebauthnIdentity: AddingWebauthnIdentity,
        @Mock authenticationService: AuthenticationService
    ) {
        this.addingWebauthnIdentity = addingWebauthnIdentity
        this.authenticationService = authenticationService
    }

    private fun withConfiguredTestApp(block: suspend ApplicationTestBuilder.() -> Unit) = withTestApp({
        bindInstance(overrides = true) { addingWebauthnIdentity }
        bindInstance(overrides = true) { authenticationService }
    }, block)

    @Test
    fun `when adding webauthn identity then returns http no content`() = withConfiguredTestApp {
        // given
        val userId = UserIdFixture.arbitrary()

        val attestationObject = byteArrayOf(1, 2, 3)
        val clientDataJSON = byteArrayOf(4, 5, 6)
        val requestBody = WebauthnIdentityAdditionDetails(attestationObject, clientDataJSON)

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        whenever(
            addingWebauthnIdentity.addWebauthnIdentity(
                eq(actingUser.asMember()),
                any(),
                eq(userId),
                eq(attestationObject),
                eq(clientDataJSON)
            )
        ).thenReturn(None)

        // when
        val response = c().post("/api/v1/user/${userId.serialize()}/identity/webauthn/response") {
            basicAuth(username, password)
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NoContent)
        assertThat(response.bodyAsText()).isEmpty()
    }

    @Test
    fun `given no authentication when adding webauthn identity then returns http unauthorized`() =
        withConfiguredTestApp {
            // given
            val userId = UserIdFixture.arbitrary()

            val attestationObject = byteArrayOf(1, 2, 3)
            val clientDataJSON = byteArrayOf(4, 5, 6)
            val requestBody = WebauthnIdentityAdditionDetails(attestationObject, clientDataJSON)

            // when
            val response = c().post("/api/v1/user/${userId.serialize()}/identity/webauthn/response") {
                basicAuth(username, password)
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            // then
            assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
        }

    @Test
    fun `given no body when adding webauthn identity then returns http bad request`() = withConfiguredTestApp {
        // given
        val userId = UserIdFixture.arbitrary()

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        // when
        val response = c().post("/api/v1/user/${userId.serialize()}/identity/webauthn/response") {
            basicAuth(username, password)
            contentType(ContentType.Application.Json)
            // empty body
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.BadRequest)
    }

    @Test
    fun `given invalid user id when adding webauthn identity then returns http bad request`() = withConfiguredTestApp {
        // given
        val invalidUserId = "invalidUserId"

        val attestationObject = byteArrayOf(1, 2, 3)
        val clientDataJSON = byteArrayOf(4, 5, 6)
        val requestBody = WebauthnIdentityAdditionDetails(attestationObject, clientDataJSON)

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        // when
        val response = c().post("/api/v1/user/${invalidUserId}/identity/webauthn/response") {
            basicAuth(username, password)
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.BadRequest)
    }

    @Test
    fun `given domain error when adding webauthn identity then returns mapped error`() = withConfiguredTestApp {
        // given
        val userId = UserIdFixture.arbitrary()

        val attestationObject = byteArrayOf(1, 2, 3)
        val clientDataJSON = byteArrayOf(4, 5, 6)
        val requestBody = WebauthnIdentityAdditionDetails(attestationObject, clientDataJSON)

        val correlationId = CorrelationIdFixture.arbitrary()
        val error = Error.WebauthnError("msg", correlationId)

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        whenever(
            addingWebauthnIdentity.addWebauthnIdentity(
                eq(actingUser.asMember()),
                any(),
                eq(userId),
                eq(attestationObject),
                eq(clientDataJSON)
            )
        ).thenReturn(error.some())

        // when
        val response = c().post("/api/v1/user/${userId.serialize()}/identity/webauthn/response") {
            basicAuth(username, password)
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.UnprocessableEntity)
        assertThat(response.body<cloud.fabX.fabXaccess.common.rest.Error>())
            .isError(
                "WebauthnError",
                "msg",
                correlationId = correlationId.serialize()
            )
    }
}