package cloud.fabX.fabXaccess.user.rest

import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.application.toHex
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.rest.c
import cloud.fabX.fabXaccess.common.rest.isError
import cloud.fabX.fabXaccess.common.rest.withTestApp
import cloud.fabX.fabXaccess.user.application.RemovingWebauthnIdentity
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import io.ktor.client.call.body
import io.ktor.client.request.basicAuth
import io.ktor.client.request.delete
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
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
internal class UserControllerRemoveWebauthnIdentityTest {
    private lateinit var removingWebauthnIdentity: RemovingWebauthnIdentity
    private lateinit var authenticationService: AuthenticationService

    private val username = "some.one"
    private val password = "supersecret123"

    private val actingUser = UserFixture.arbitrary(isAdmin = true)

    @BeforeEach
    fun `configure WebModule`(
        @Mock removingWebauthnIdentity: RemovingWebauthnIdentity,
        @Mock authenticationService: AuthenticationService
    ) {
        this.removingWebauthnIdentity = removingWebauthnIdentity
        this.authenticationService = authenticationService
    }

    private fun withConfiguredTestApp(block: suspend ApplicationTestBuilder.() -> Unit) = withTestApp({
        bindInstance(overrides = true) { removingWebauthnIdentity }
        bindInstance(overrides = true) { authenticationService }
    }, block)

    @Test
    fun `when removing webauthn identity then returns http no content`() = withConfiguredTestApp {
        // given
        val userId = UserIdFixture.arbitrary()
        val credentialId = byteArrayOf(1, 2, 3, 4, 5)
        val credentialIdHex = credentialId.toHex()

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        whenever(
            removingWebauthnIdentity.removeWebauthnIdentity(
                eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                any(),
                eq(userId),
                eq(credentialId)
            )
        ).thenReturn(Unit.right())

        // when
        val response = c().delete("/api/v1/user/${userId.serialize()}/identity/webauthn/$credentialIdHex") {
            basicAuth(username, password)
        }

        //  then
        assertThat(response.status).isEqualTo(HttpStatusCode.NoContent)
        assertThat(response.bodyAsText()).isEmpty()
    }

    @Test
    fun `given invalid user id when removing webauthn identity then returns http bad request`() =
        withConfiguredTestApp {
            // given
            val invalidUserId = "invalidUserId"

            whenever(authenticationService.basic(UserPasswordCredential(username, password)))
                .thenReturn(UserPrincipal(actingUser))

            // when
            val response = c().delete("/api/v1/user/$invalidUserId/identity/webauthn/010203") {
                basicAuth(username, password)
            }

            // then
            assertThat(response.status).isEqualTo(HttpStatusCode.BadRequest)
            assertThat(response.body<String>()).isEqualTo("Required UUID parameter \"id\" not given or invalid.")
        }

    @Test
    fun `given invalid credential id when removing webauthn identity then returns http bad request`() =
        withConfiguredTestApp {
            // given
            val userId = UserIdFixture.arbitrary()
            val invalidCredentialId = "123XYZ"

            whenever(authenticationService.basic(UserPasswordCredential(username, password)))
                .thenReturn(UserPrincipal(actingUser))


            // when
            val response = c().delete("/api/v1/user/${userId.serialize()}/identity/webauthn/$invalidCredentialId") {
                basicAuth(username, password)
            }

            // then
            assertThat(response.status).isEqualTo(HttpStatusCode.BadRequest)
            assertThat(response.body<String>()).isEqualTo(
                "Required hex string parameter \"credentialId\" not given or invalid."
            )
        }

    @Test
    fun `given domain error when removing webauthn identity then returns mapped error`() = withConfiguredTestApp {
        // given
        val userId = UserIdFixture.arbitrary()
        val credentialId = byteArrayOf(1, 2, 3, 4, 5)
        val credentialIdHex = credentialId.toHex()

        val correlationId = CorrelationIdFixture.arbitrary()
        val error = Error.UserIdentityNotFound("msg", mapOf("credentialId" to credentialIdHex), correlationId)

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        whenever(
            removingWebauthnIdentity.removeWebauthnIdentity(
                eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                any(),
                eq(userId),
                eq(credentialId)
            )
        ).thenReturn(error.left())

        // when
        val response = c().delete("/api/v1/user/${userId.serialize()}/identity/webauthn/$credentialIdHex") {
            basicAuth(username, password)
        }

        //  then
        assertThat(response.status).isEqualTo(HttpStatusCode.UnprocessableEntity)
        assertThat(response.body<cloud.fabX.fabXaccess.common.rest.Error>())
            .isError(
                "UserIdentityNotFound",
                "msg",
                mapOf("credentialId" to credentialIdHex),
                correlationId.serialize()
            )
    }
}