package cloud.fabX.fabXaccess.user.rest

import arrow.core.getOrElse
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
import cloud.fabX.fabXaccess.user.application.RemovingCardIdentity
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
internal class UserControllerRemoveCardIdentityTest {
    private lateinit var removingCardIdentity: RemovingCardIdentity
    private lateinit var authenticationService: AuthenticationService

    private val username = "some.one"
    private val password = "supersecret123"

    private val actingUser = UserFixture.arbitrary(isAdmin = true)

    @BeforeEach
    fun `configure WebModule`(
        @Mock removingCardIdentity: RemovingCardIdentity,
        @Mock authenticationService: AuthenticationService
    ) {
        this.removingCardIdentity = removingCardIdentity
        this.authenticationService = authenticationService
    }

    private fun withConfiguredTestApp(block: suspend ApplicationTestBuilder.() -> Unit) = withTestApp({
        bindInstance(overrides = true) { removingCardIdentity }
        bindInstance(overrides = true) { authenticationService }
    }, block)

    @Test
    fun `when removing card identity then returns http no content`() = withConfiguredTestApp {
        // given
        val userId = UserIdFixture.arbitrary()
        val cardId = "11223344556677"

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        whenever(
            removingCardIdentity.removeCardIdentity(
                eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                any(),
                eq(userId),
                eq(cardId)
            )
        ).thenReturn(Unit.right())

        // when
        val response = c().delete("/api/v1/user/${userId.serialize()}/identity/card/$cardId") {
            basicAuth(username, password)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NoContent)
        assertThat(response.bodyAsText()).isEmpty()
    }

    @Test
    fun `given no admin authentication when removing card identity then returns http forbidden`() =
        withConfiguredTestApp {
            // given
            val message = "msg123"
            val error = Error.UserNotAdmin(message)

            whenever(authenticationService.basic(UserPasswordCredential(username, password)))
                .thenReturn(ErrorPrincipal(error))

            // when
            val response =
                c().delete("/api/v1/user/${UserIdFixture.arbitrary().serialize()}/identity/card/AABB1122CCDD33") {
                    basicAuth(username, password)
                }

            // then
            assertThat(response.status).isEqualTo(HttpStatusCode.Forbidden)
            assertThat(response.body<cloud.fabX.fabXaccess.common.rest.Error>())
                .isError(
                    "UserNotAdmin",
                    message
                )
        }

    @Test
    fun `given invalid user id when removing card identity then returns http bad request`() =
        withConfiguredTestApp {
            // given
            val invalidUserId = "invalidUserId"

            whenever(authenticationService.basic(UserPasswordCredential(username, password)))
                .thenReturn(UserPrincipal(actingUser))

            // when
            val response = c().delete("/api/v1/user/$invalidUserId/identity/card/AA11BB22CC33DD") {
                basicAuth(username, password)
            }

            // then
            assertThat(response.status).isEqualTo(HttpStatusCode.BadRequest)
            assertThat(response.body<String>()).isEqualTo("Required UUID parameter \"id\" not given or invalid.")
        }

    @Test
    fun `given domain error when removing card identity then returns mapped error`() =
        withConfiguredTestApp {
            // given
            val userId = UserIdFixture.arbitrary()
            val cardId = "AABBCC11223344"

            val correlationId = CorrelationIdFixture.arbitrary()
            val error = Error.UserIdentityNotFound("msg", mapOf("cardId" to cardId), correlationId)

            whenever(authenticationService.basic(UserPasswordCredential(username, password)))
                .thenReturn(UserPrincipal(actingUser))

            whenever(
                removingCardIdentity.removeCardIdentity(
                    eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                    any(),
                    eq(userId),
                    eq(cardId)
                )
            ).thenReturn(error.left())

            // when
            val response = c().delete("/api/v1/user/${userId.serialize()}/identity/card/$cardId") {
                basicAuth(username, password)
            }

            // then
            assertThat(response.status).isEqualTo(HttpStatusCode.UnprocessableEntity)
            assertThat(response.body<cloud.fabX.fabXaccess.common.rest.Error>())
                .isError(
                    "UserIdentityNotFound",
                    "msg",
                    mapOf("cardId" to cardId),
                    correlationId.serialize()
                )
        }
}