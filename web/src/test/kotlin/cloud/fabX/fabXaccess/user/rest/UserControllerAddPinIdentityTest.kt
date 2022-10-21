package cloud.fabX.fabXaccess.user.rest

import arrow.core.None
import arrow.core.getOrElse
import arrow.core.some
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.rest.c
import cloud.fabX.fabXaccess.common.rest.isError
import cloud.fabX.fabXaccess.common.rest.withTestApp
import cloud.fabX.fabXaccess.user.application.AddingPinIdentity
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
internal class UserControllerAddPinIdentityTest {
    private lateinit var addingPinIdentity: AddingPinIdentity
    private lateinit var authenticationService: AuthenticationService

    private val username = "some.one"
    private val password = "supersecret123"

    private val actingUser = UserFixture.arbitrary(isAdmin = true)

    @BeforeEach
    fun `configure WebModule`(
        @Mock addingPinIdentity: AddingPinIdentity,
        @Mock authenticationService: AuthenticationService
    ) {
        this.addingPinIdentity = addingPinIdentity
        this.authenticationService = authenticationService
    }

    private fun withConfiguredTestApp(block: suspend ApplicationTestBuilder.() -> Unit) = withTestApp({
        bindInstance(overrides = true) { addingPinIdentity }
        bindInstance(overrides = true) { authenticationService }
    }, block)

    @Test
    fun `when adding pin identity then returns http no content`() = withConfiguredTestApp {
        // given
        val userId = UserIdFixture.arbitrary()

        val pin = "8765"
        val requestBody = PinIdentityAdditionDetails(pin)

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        whenever(
            addingPinIdentity.addPinIdentity(
                eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                any(),
                eq(userId),
                eq(pin)
            )
        ).thenReturn(None)

        // when
        val response = c().post("/api/v1/user/${userId.serialize()}/identity/pin") {
            basicAuth(username, password)
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NoContent)
        assertThat(response.bodyAsText()).isEmpty()
    }

    @Test
    fun `given no admin authentication when adding pin identity then returns http forbidden`() = withConfiguredTestApp {
        // given
        val message = "msg123"
        val error = Error.UserNotAdmin(message)

        val requestBody = PinIdentityAdditionDetails("1234")

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(ErrorPrincipal(error))

        // when
        val response = c().post("/api/v1/user/${UserIdFixture.arbitrary().serialize()}/identity/pin") {
            basicAuth(username, password)
            contentType(ContentType.Application.Json)
            setBody(requestBody)
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
    fun `given no body when adding pin identity then returns http bad request`() = withConfiguredTestApp {
        // given
        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        // when
        val response = c().post("/api/v1/user/${UserIdFixture.arbitrary().serialize()}/identity/pin") {
            basicAuth(username, password)
            contentType(ContentType.Application.Json)
            // empty body
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.BadRequest)
    }

    @Test
    fun `given invalid user id when adding pin identity then returns http bad request`() = withConfiguredTestApp {
        // given
        val invalidUserId = "invalidUserId"

        val requestBody = PinIdentityAdditionDetails("1234")

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        // when
        val response = c().post("/api/v1/user/$invalidUserId/identity/pin") {
            basicAuth(username, password)
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.BadRequest)
        assertThat(response.body<String>())
            .isEqualTo("Required UUID parameter \"id\" not given or invalid.")
    }

    @Test
    fun `given domain error when adding pin identity then returns mapped error`() = withConfiguredTestApp {
        // given
        val userId = UserIdFixture.arbitrary()

        val pin = "8765"
        val requestBody = PinIdentityAdditionDetails(pin)

        val correlationId = CorrelationIdFixture.arbitrary()
        val error = Error.PinIdentityAlreadyFound("msg678", correlationId)

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        whenever(
            addingPinIdentity.addPinIdentity(
                eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                any(),
                eq(userId),
                eq(pin)
            )
        ).thenReturn(error.some())

        // when
        val response = c().post("/api/v1/user/${userId.serialize()}/identity/pin") {
            basicAuth(username, password)
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.UnprocessableEntity)
        assertThat(response.body<cloud.fabX.fabXaccess.common.rest.Error>())
            .isError(
                "PinIdentityAlreadyFound",
                "msg678",
                correlationId = correlationId.serialize()
            )
    }
}