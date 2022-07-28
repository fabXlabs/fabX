package cloud.fabX.fabXaccess.user.rest

import arrow.core.None
import arrow.core.getOrElse
import arrow.core.some
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.rest.addBasicAuth
import cloud.fabX.fabXaccess.common.rest.isJson
import cloud.fabX.fabXaccess.common.rest.withTestApp
import cloud.fabX.fabXaccess.user.application.AddingUsernamePasswordIdentity
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import io.ktor.auth.UserPasswordCredential
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.util.InternalAPI
import kotlinx.serialization.ExperimentalSerializationApi
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

@InternalAPI
@ExperimentalSerializationApi
@MockitoSettings
internal class UserControllerAddUsernamePasswordIdentityTest {
    private lateinit var addingUsernamePasswordIdentity: AddingUsernamePasswordIdentity
    private lateinit var authenticationService: AuthenticationService

    private val username = "some.one"
    private val password = "supersecret123"

    private val actingUser = UserFixture.arbitrary(isAdmin = true)

    @BeforeEach
    fun `configure RestModule`(
        @Mock addingUsernamePasswordIdentity: AddingUsernamePasswordIdentity,
        @Mock authenticationService: AuthenticationService
    ) {
        this.addingUsernamePasswordIdentity = addingUsernamePasswordIdentity
        this.authenticationService = authenticationService
    }

    private fun withConfiguredTestApp(block: TestApplicationEngine.() -> Unit) = withTestApp({
        bindInstance(overrides = true) { addingUsernamePasswordIdentity }
        bindInstance(overrides = true) { authenticationService }
    }, block)

    @Test
    fun `when adding username password identity then returns http no content`() = withConfiguredTestApp {
        // given
        val userId = UserIdFixture.arbitrary()

        val requestBody = UsernamePasswordIdentity(
            "username123",
            "password123"
        )

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        whenever(
            addingUsernamePasswordIdentity.addUsernamePasswordIdentity(
                eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                any(),
                eq(userId),
                eq("username123"),
                eq("wlmFWvSHIVH3oFFS2wcoNRrQ+76pntkclC/yzYyvVck=")
            )
        ).thenReturn(None)

        // when
        val result = handleRequest(HttpMethod.Post, "/api/v1/user/${userId.serialize()}/identity/username-password") {
            addBasicAuth(username, password)
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(requestBody))
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.NoContent)
        assertThat(result.response.content).isNull()
    }

    @Test
    fun `given no admin authentication when adding username password identity then returns http forbidden`() =
        withConfiguredTestApp {
            // given
            val requestBody = UsernamePasswordIdentity(
                "username123",
                "password123"
            )

            val message = "msg123"
            val error = Error.UserNotAdmin(message)

            whenever(authenticationService.basic(UserPasswordCredential(username, password)))
                .thenReturn(ErrorPrincipal(error))

            // when
            val result = handleRequest(
                HttpMethod.Post,
                "/api/v1/user/${UserIdFixture.arbitrary().serialize()}/identity/username-password"
            ) {
                addBasicAuth(username, password)
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(Json.encodeToString(requestBody))
            }

            // then
            assertThat(result.response.status()).isEqualTo(HttpStatusCode.Forbidden)
            assertThat(result.response.content)
                .isNotNull()
                .isJson<cloud.fabX.fabXaccess.common.rest.Error>()
                .isEqualTo(
                    cloud.fabX.fabXaccess.common.rest.Error(
                        "UserNotAdmin",
                        message,
                        mapOf()
                    )
                )
        }

    @Test
    fun `given no body when adding username password identity then returns http unprocessable entity`() =
        withConfiguredTestApp {
            // given
            whenever(authenticationService.basic(UserPasswordCredential(username, password)))
                .thenReturn(UserPrincipal(actingUser))

            // when
            val result = handleRequest(
                HttpMethod.Post,
                "/api/v1/user/${UserIdFixture.arbitrary().serialize()}/identity/username-password"
            ) {
                addBasicAuth(username, password)
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                // empty body
            }

            // then
            assertThat(result.response.status()).isEqualTo(HttpStatusCode.UnprocessableEntity)
        }

    @Test
    fun `given invalid user id when adding username password identity then returns http bad request`() =
        withConfiguredTestApp {
            // given
            val invalidUserId = "invalidUserId"

            val requestBody = UsernamePasswordIdentity(
                "username123",
                "password123"
            )

            whenever(authenticationService.basic(UserPasswordCredential(username, password)))
                .thenReturn(UserPrincipal(actingUser))

            // when
            val result = handleRequest(
                HttpMethod.Post,
                "/api/v1/user/$invalidUserId/identity/username-password"
            ) {
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
    fun `given domain error when adding username password identity then returns mapped error`() =
        withConfiguredTestApp {
            val userId = UserIdFixture.arbitrary()

            val requestBody = UsernamePasswordIdentity(
                "username123",
                "password123"
            )

            val error = Error.UsernamePasswordIdentityAlreadyFound("some message")

            whenever(authenticationService.basic(UserPasswordCredential(username, password)))
                .thenReturn(UserPrincipal(actingUser))

            whenever(
                addingUsernamePasswordIdentity.addUsernamePasswordIdentity(
                    eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                    any(),
                    eq(userId),
                    eq("username123"),
                    eq("wlmFWvSHIVH3oFFS2wcoNRrQ+76pntkclC/yzYyvVck=")
                )
            ).thenReturn(error.some())

            // when
            val result =
                handleRequest(HttpMethod.Post, "/api/v1/user/${userId.serialize()}/identity/username-password") {
                    addBasicAuth(username, password)
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(Json.encodeToString(requestBody))
                }

            // then
            assertThat(result.response.status()).isEqualTo(HttpStatusCode.UnprocessableEntity)
            assertThat(result.response.content)
                .isNotNull()
                .isJson<cloud.fabX.fabXaccess.common.rest.Error>()
                .isEqualTo(
                    cloud.fabX.fabXaccess.common.rest.Error(
                        "UsernamePasswordIdentityAlreadyFound",
                        "some message",
                        mapOf()
                    )
                )
        }
}