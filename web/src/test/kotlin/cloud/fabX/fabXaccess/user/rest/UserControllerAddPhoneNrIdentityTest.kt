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
import cloud.fabX.fabXaccess.user.application.AddingPhoneNrIdentity
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
internal class UserControllerAddPhoneNrIdentityTest {
    private lateinit var addingPhoneNrIdentity: AddingPhoneNrIdentity
    private lateinit var authenticationService: AuthenticationService

    private val username = "some.one"
    private val password = "supersecret123"

    private val actingUser = UserFixture.arbitrary(isAdmin = true)

    @BeforeEach
    fun `configure WebModule`(
        @Mock addingPhoneNrIdentity: AddingPhoneNrIdentity,
        @Mock authenticationService: AuthenticationService
    ) {
        this.addingPhoneNrIdentity = addingPhoneNrIdentity
        this.authenticationService = authenticationService
    }

    private fun withConfiguredTestApp(block: suspend ApplicationTestBuilder.() -> Unit) = withTestApp({
        bindInstance(overrides = true) { addingPhoneNrIdentity }
        bindInstance(overrides = true) { authenticationService }
    }, block)

    @Test
    fun `when adding phone number identity then returns http no content`() = withConfiguredTestApp {
        // given
        val userId = UserIdFixture.arbitrary()

        val phoneNr = "+49123456789"
        val requestBody = PhoneNrIdentity(phoneNr)

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        whenever(
            addingPhoneNrIdentity.addPhoneNrIdentity(
                eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                any(),
                eq(userId),
                eq(phoneNr),
            )
        ).thenReturn(None)

        // when
        val response = c().post("/api/v1/user/${userId.serialize()}/identity/phone") {
            basicAuth(username, password)
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NoContent)
        assertThat(response.bodyAsText()).isEmpty()
    }

    @Test
    fun `given no admin authentication when adding phone number identity then returns http forbidden`() =
        withConfiguredTestApp {
            // given
            val requestBody = PhoneNrIdentity("+49123456789")

            val message = "msg123"
            val error = Error.UserNotAdmin(message)

            whenever(authenticationService.basic(UserPasswordCredential(username, password)))
                .thenReturn(ErrorPrincipal(error))

            // when
            val response = c().post("/api/v1/user/${UserIdFixture.arbitrary().serialize()}/identity/phone") {
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
    fun `given no body when adding phone number identity then returns http unprocessable entity`() =
        withConfiguredTestApp {
            // given
            whenever(authenticationService.basic(UserPasswordCredential(username, password)))
                .thenReturn(UserPrincipal(actingUser))

            // when
            val response = c().post("/api/v1/user/${UserIdFixture.arbitrary().serialize()}/identity/phone") {
                basicAuth(username, password)
                contentType(ContentType.Application.Json)
                // empty body
            }

            // then
            assertThat(response.status).isEqualTo(HttpStatusCode.BadRequest)
        }

    @Test
    fun `given invalid user id when adding phone number identity then returns http bad request`() =
        withConfiguredTestApp {
            // given
            val invalidUserId = "invalidUserId"

            val requestBody = PhoneNrIdentity("+49123456789")

            whenever(authenticationService.basic(UserPasswordCredential(username, password)))
                .thenReturn(UserPrincipal(actingUser))

            // when
            val response = c().post("/api/v1/user/$invalidUserId/identity/phone") {
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
    fun `given domain error when adding phone number identity then returns mapped error`() = withConfiguredTestApp {
        // given
        val userId = UserIdFixture.arbitrary()

        val phoneNr = "+49123456789"
        val requestBody = PhoneNrIdentity(phoneNr)

        val correlationId = CorrelationIdFixture.arbitrary()
        val error = Error.PhoneNrAlreadyInUse("msg678", correlationId)

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        whenever(
            addingPhoneNrIdentity.addPhoneNrIdentity(
                eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                any(),
                eq(userId),
                eq(phoneNr),
            )
        ).thenReturn(error.some())

        // when
        val response = c().post("/api/v1/user/${userId.serialize()}/identity/phone") {
            basicAuth(username, password)
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.UnprocessableEntity)
        assertThat(response.body<cloud.fabX.fabXaccess.common.rest.Error>())
            .isError(
                "PhoneNrAlreadyInUse",
                "msg678",
                correlationId = correlationId.serialize()
            )
    }
}