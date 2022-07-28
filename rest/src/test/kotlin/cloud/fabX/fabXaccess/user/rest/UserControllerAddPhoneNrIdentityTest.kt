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
import cloud.fabX.fabXaccess.user.application.AddingPhoneNrIdentity
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
internal class UserControllerAddPhoneNrIdentityTest {
    private lateinit var addingPhoneNrIdentity: AddingPhoneNrIdentity
    private lateinit var authenticationService: AuthenticationService

    private val username = "some.one"
    private val password = "supersecret123"

    private val actingUser = UserFixture.arbitrary(isAdmin = true)

    @BeforeEach
    fun `configure RestModule`(
        @Mock addingPhoneNrIdentity: AddingPhoneNrIdentity,
        @Mock authenticationService: AuthenticationService
    ) {
        this.addingPhoneNrIdentity = addingPhoneNrIdentity
        this.authenticationService = authenticationService
    }

    private fun withConfiguredTestApp(block: TestApplicationEngine.() -> Unit) = withTestApp({
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
        val result = handleRequest(HttpMethod.Post, "/api/v1/user/${userId.serialize()}/identity/phone") {
            addBasicAuth(username, password)
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(requestBody))
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.NoContent)
        assertThat(result.response.content).isNull()
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
            val result = handleRequest(
                HttpMethod.Post,
                "/api/v1/user/${UserIdFixture.arbitrary().serialize()}/identity/phone"
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
    fun `given no body when adding phone number identity then returns http unprocessable entity`() =
        withConfiguredTestApp {
            // given
            whenever(authenticationService.basic(UserPasswordCredential(username, password)))
                .thenReturn(UserPrincipal(actingUser))

            // when
            val result = handleRequest(
                HttpMethod.Post,
                "/api/v1/user/${UserIdFixture.arbitrary().serialize()}/identity/phone"
            ) {
                addBasicAuth(username, password)
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                // empty body
            }

            // then
            assertThat(result.response.status()).isEqualTo(HttpStatusCode.UnprocessableEntity)
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
            val result = handleRequest(HttpMethod.Post, "/api/v1/user/$invalidUserId/identity/phone") {
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
    fun `given domain error when adding phone number identity then returns mapped error`() = withConfiguredTestApp {
        // given
        val userId = UserIdFixture.arbitrary()

        val phoneNr = "+49123456789"
        val requestBody = PhoneNrIdentity(phoneNr)

        val error = Error.PhoneNrAlreadyInUse("msg678")

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
        val result = handleRequest(HttpMethod.Post, "/api/v1/user/${userId.serialize()}/identity/phone") {
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
                    "PhoneNrAlreadyInUse",
                    "msg678",
                    mapOf()
                )
            )
    }
}