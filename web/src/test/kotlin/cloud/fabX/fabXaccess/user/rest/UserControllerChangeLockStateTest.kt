package cloud.fabX.fabXaccess.user.rest

import arrow.core.None
import arrow.core.getOrElse
import arrow.core.some
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.ErrorFixture
import cloud.fabX.fabXaccess.common.rest.ChangeableValue
import cloud.fabX.fabXaccess.common.rest.addBasicAuth
import cloud.fabX.fabXaccess.common.rest.isError
import cloud.fabX.fabXaccess.common.rest.withTestApp
import cloud.fabX.fabXaccess.user.application.ChangingUser
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.UserPasswordCredential
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.kodein.di.bindInstance
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever

@MockitoSettings
internal class UserControllerChangeLockStateTest {
    private lateinit var changingUser: ChangingUser
    private lateinit var authenticationService: AuthenticationService

    private val username = "some.one"
    private val password = "supersecret123"

    private val actingUser = UserFixture.arbitrary(isAdmin = true)

    @BeforeEach
    fun `configure WebModule`(
        @Mock changingUser: ChangingUser,
        @Mock authenticationService: AuthenticationService
    ) {
        this.changingUser = changingUser
        this.authenticationService = authenticationService
    }

    private fun withConfiguredTestApp(block: suspend TestApplicationEngine.() -> Unit) = withTestApp({
        bindInstance(overrides = true) { changingUser }
        bindInstance(overrides = true) { authenticationService }
    }, block)

    companion object {
        @JvmStatic
        fun userLockDetailsProvider() = listOf(
            Arguments.of(
                UserLockDetails(
                    null,
                    null
                ),
                cloud.fabX.fabXaccess.common.model.ChangeableValue.LeaveAsIs,
                cloud.fabX.fabXaccess.common.model.ChangeableValue.LeaveAsIs
            ),
            Arguments.of(
                UserLockDetails(
                    ChangeableValue(true),
                    ChangeableValue("some notes")
                ),
                cloud.fabX.fabXaccess.common.model.ChangeableValue.ChangeToValueBoolean(true),
                cloud.fabX.fabXaccess.common.model.ChangeableValue.ChangeToValueString("some notes")
            ),
            Arguments.of(
                UserLockDetails(
                    ChangeableValue(false),
                    null
                ),
                cloud.fabX.fabXaccess.common.model.ChangeableValue.ChangeToValueBoolean(false),
                cloud.fabX.fabXaccess.common.model.ChangeableValue.LeaveAsIs
            ),
            Arguments.of(
                UserLockDetails(
                    ChangeableValue(false),
                    ChangeableValue(null)
                ),
                cloud.fabX.fabXaccess.common.model.ChangeableValue.ChangeToValueBoolean(false),
                cloud.fabX.fabXaccess.common.model.ChangeableValue.ChangeToValueOptionalString(null)
            )
        )
    }

    @ParameterizedTest
    @MethodSource("userLockDetailsProvider")
    fun `when changing user lock state then returns http no content`(
        requestBody: UserLockDetails,
        locked: cloud.fabX.fabXaccess.common.model.ChangeableValue<Boolean>,
        notes: cloud.fabX.fabXaccess.common.model.ChangeableValue<String>,
    ) = withConfiguredTestApp {
        // given
        val id = UserIdFixture.arbitrary()

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        whenever(
            changingUser.changeLockState(
                eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                any(),
                eq(id),
                eq(locked),
                eq(notes)
            )
        ).thenReturn(None)

        // when
        val result = handleRequest(HttpMethod.Put, "/api/v1/user/${id.serialize()}/lock") {
            addBasicAuth(username, password)
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(requestBody))
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.NoContent)
        assertThat(result.response.content).isNull()
    }

    @Test
    fun `given non-admin authentication when changing user lock state then returns http forbidden`() =
        withConfiguredTestApp {
            // given
            val requestBody = UserLockDetails(
                null,
                null
            )

            val message = "error message"
            val error = Error.UserNotAdmin(message)

            whenever(authenticationService.basic(UserPasswordCredential(username, password)))
                .thenReturn(ErrorPrincipal(error))

            // when
            val result = handleRequest(HttpMethod.Put, "/api/v1/user/${UserIdFixture.arbitrary().serialize()}/lock") {
                addBasicAuth(username, password)
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(Json.encodeToString(requestBody))
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
    fun `given no body when changing user lock state then returns http unprocessable entity`() =
        withConfiguredTestApp {
            // given
            whenever(authenticationService.basic(UserPasswordCredential(username, password)))
                .thenReturn(UserPrincipal(actingUser))

            // when
            val result = handleRequest(HttpMethod.Put, "/api/v1/user/${UserIdFixture.arbitrary().serialize()}/lock") {
                addBasicAuth(username, password)
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                // empty body
            }

            // then
            assertThat(result.response.status()).isEqualTo(HttpStatusCode.UnprocessableEntity)
        }

    @Test
    fun `given invalid user id when changing user lock state then returns http bad request`() =
        withConfiguredTestApp {
            // given
            val invalidUserId = "invalidUserId"

            val requestBody = UserLockDetails(
                null,
                null
            )

            whenever(authenticationService.basic(UserPasswordCredential(username, password)))
                .thenReturn(UserPrincipal(actingUser))

            // when
            val result = handleRequest(HttpMethod.Put, "/api/v1/user/$invalidUserId/lock") {
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
    fun `given domain error when changing user lock state then returns mapped domain error`() =
        withConfiguredTestApp {
            // given
            val id = UserIdFixture.arbitrary()

            val requestBody = UserLockDetails(
                null,
                null
            )

            val error = ErrorFixture.arbitrary()

            whenever(authenticationService.basic(UserPasswordCredential(username, password)))
                .thenReturn(UserPrincipal(actingUser))

            whenever(
                changingUser.changeLockState(
                    eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                    any(),
                    eq(id),
                    eq(cloud.fabX.fabXaccess.common.model.ChangeableValue.LeaveAsIs),
                    eq(cloud.fabX.fabXaccess.common.model.ChangeableValue.LeaveAsIs)
                )
            ).thenReturn(error.some())

            // when
            val result = handleRequest(HttpMethod.Put, "/api/v1/user/${id.serialize()}/lock") {
                addBasicAuth(username, password)
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(Json.encodeToString(requestBody))
            }

            // then
            assertThat(result.response.status()).isEqualTo(HttpStatusCode.UnprocessableEntity)
            assertThat(result.response.content)
                .isError(
                    "VersionConflict",
                    "some message"
                )
        }
}