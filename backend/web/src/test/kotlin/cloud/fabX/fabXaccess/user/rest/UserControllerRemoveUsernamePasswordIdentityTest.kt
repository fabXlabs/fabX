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
import cloud.fabX.fabXaccess.user.application.RemovingUsernamePasswordIdentity
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.UserPasswordCredential
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.kodein.di.bindInstance
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever

@MockitoSettings
internal class UserControllerRemoveUsernamePasswordIdentityTest {
    private lateinit var removingUsernamePasswordIdentity: RemovingUsernamePasswordIdentity
    private lateinit var authenticationService: AuthenticationService

    private val username = "some.one"
    private val password = "supersecret123"

    private val actingUser = UserFixture.arbitrary(isAdmin = true)

    @BeforeEach
    fun `configure WebModule`(
        @Mock removingUsernamePasswordIdentity: RemovingUsernamePasswordIdentity,
        @Mock authenticationService: AuthenticationService
    ) {
        this.removingUsernamePasswordIdentity = removingUsernamePasswordIdentity
        this.authenticationService = authenticationService
    }

    private fun withConfiguredTestApp(block: suspend TestApplicationEngine.() -> Unit) = withTestApp({
        bindInstance(overrides = true) { removingUsernamePasswordIdentity }
        bindInstance(overrides = true) { authenticationService }
    }, block)

    @Test
    fun `when removing username password identity then returns http no content`() = withConfiguredTestApp {
        // given
        val userId = UserIdFixture.arbitrary()
        val name = "name123"

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        whenever(
            removingUsernamePasswordIdentity.removeUsernamePasswordIdentity(
                eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                any(),
                eq(userId),
                eq(name)
            )
        ).thenReturn(None)

        // when
        val result = handleRequest(
            HttpMethod.Delete,
            "/api/v1/user/${userId.serialize()}/identity/username-password/$name"
        ) {
            addBasicAuth(username, password)
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.NoContent)
        assertThat(result.response.content).isNull()
    }

    @Test
    fun `given no admin authentication when removing username password identity then returns http forbidden`() =
        withConfiguredTestApp {
            // given
            val message = "msg123"
            val error = Error.UserNotAdmin(message)

            whenever(authenticationService.basic(UserPasswordCredential(username, password)))
                .thenReturn(ErrorPrincipal(error))

            // when
            val result = handleRequest(
                HttpMethod.Delete,
                "/api/v1/user/${UserIdFixture.arbitrary().serialize()}/identity/username-password/username123"
            ) {
                addBasicAuth(username, password)
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
    fun `given invalid user id when removing username password identity then returns http bad request`() =
        withConfiguredTestApp {
            // given
            val invalidUserId = "invalidUserId"

            whenever(authenticationService.basic(UserPasswordCredential(username, password)))
                .thenReturn(UserPrincipal(actingUser))

            // when
            val result = handleRequest(
                HttpMethod.Delete,
                "/api/v1/user/$invalidUserId/identity/username-password/username123"
            ) {
                addBasicAuth(username, password)
            }

            // then
            assertThat(result.response.status()).isEqualTo(HttpStatusCode.BadRequest)
            assertThat(result.response.content)
                .isNotNull()
                .isEqualTo("Required UUID parameter \"id\" not given or invalid.")
        }

    @Test
    fun `given domain error when removing username password identity then returns http bad request`() =
        withConfiguredTestApp {
            // given
            val userId = UserIdFixture.arbitrary()
            val name = "name123"

            val correlationId = CorrelationIdFixture.arbitrary()
            val error = Error.UserIdentityNotFound("msg", mapOf("username" to name), correlationId)

            whenever(authenticationService.basic(UserPasswordCredential(username, password)))
                .thenReturn(UserPrincipal(actingUser))

            whenever(
                removingUsernamePasswordIdentity.removeUsernamePasswordIdentity(
                    eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                    any(),
                    eq(userId),
                    eq(name)
                )
            ).thenReturn(error.some())

            // when
            val result = handleRequest(
                HttpMethod.Delete,
                "/api/v1/user/${userId.serialize()}/identity/username-password/$name"
            ) {
                addBasicAuth(username, password)
            }

            // then
            assertThat(result.response.status()).isEqualTo(HttpStatusCode.UnprocessableEntity)
            assertThat(result.response.content)
                .isError(
                    "UserIdentityNotFound",
                    "msg",
                    mapOf("username" to name),
                    correlationId.serialize()
                )
        }
}