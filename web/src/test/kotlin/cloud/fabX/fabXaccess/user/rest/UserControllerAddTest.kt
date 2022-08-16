package cloud.fabX.fabXaccess.user.rest

import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.rest.c
import cloud.fabX.fabXaccess.common.rest.isError
import cloud.fabX.fabXaccess.common.rest.withTestApp
import cloud.fabX.fabXaccess.user.application.AddingUser
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import io.ktor.client.call.body
import io.ktor.client.request.basicAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
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

    private fun withConfiguredTestApp(block: suspend ApplicationTestBuilder.() -> Unit) = withTestApp({
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
        val response = c().post("/api/v1/user") {
            basicAuth(username, password)
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.body<String>()).isEqualTo(userId.serialize())
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
        val response = c().post("/api/v1/user") {
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
    fun `given no body when adding user then returns http unprocessable entity`() = withConfiguredTestApp {
        // given
        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        // when
        val response = c().post("/api/v1/user") {
            basicAuth(username, password)
            contentType(ContentType.Application.Json)
            // empty body
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.BadRequest)
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
        val response = c().post("/api/v1/user") {
            basicAuth(username, password)
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.UnprocessableEntity)
        assertThat(response.body<cloud.fabX.fabXaccess.common.rest.Error>())
            .isError(
                "WikiNameAlreadyInUse",
                "message 123",
                correlationId = correlationId.serialize()
            )
    }
}