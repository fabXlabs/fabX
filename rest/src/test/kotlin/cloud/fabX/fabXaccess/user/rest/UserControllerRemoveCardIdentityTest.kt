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
import cloud.fabX.fabXaccess.user.application.RemovingCardIdentity
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import io.ktor.auth.UserPasswordCredential
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.util.InternalAPI
import kotlinx.serialization.ExperimentalSerializationApi
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
internal class UserControllerRemoveCardIdentityTest {
    private lateinit var removingCardIdentity: RemovingCardIdentity
    private lateinit var authenticationService: AuthenticationService

    private val username = "some.one"
    private val password = "supersecret123"

    private val actingUser = UserFixture.arbitrary(isAdmin = true)

    @BeforeEach
    fun `configure RestModule`(
        @Mock removingCardIdentity: RemovingCardIdentity,
        @Mock authenticationService: AuthenticationService
    ) {
        this.removingCardIdentity = removingCardIdentity
        this.authenticationService = authenticationService
    }

    private fun withConfiguredTestApp(block: TestApplicationEngine.() -> Unit) = withTestApp({
        bindInstance(overrides = true) { removingCardIdentity }
        bindInstance(overrides = true) { authenticationService }
    }, block)

    @Test
    fun `when removing card identity then returns http ok`() = withConfiguredTestApp {
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
        ).thenReturn(None)

        // when
        val result = handleRequest(
            HttpMethod.Delete,
            "/api/v1/user/${userId.serialize()}/identity/card/$cardId"
        ) {
            addBasicAuth(username, password)
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.OK)
        assertThat(result.response.content).isNull()
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
            val result = handleRequest(
                HttpMethod.Delete,
                "/api/v1/user/${UserIdFixture.arbitrary().serialize()}/identity/card/AABB1122CCDD33"
            ) {
                addBasicAuth(username, password)
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
    fun `given invalid user id when removing card identity then returns http bad request`() =
        withConfiguredTestApp {
            // given
            val invalidUserId = "invalidUserId"

            whenever(authenticationService.basic(UserPasswordCredential(username, password)))
                .thenReturn(UserPrincipal(actingUser))

            // when
            val result = handleRequest(
                HttpMethod.Delete,
                "/api/v1/user/$invalidUserId/identity/card/AA11BB22CC33DD"
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
    fun `given domain error when removing card identity then returns http bad request`() =
        withConfiguredTestApp {
            // given
            val userId = UserIdFixture.arbitrary()
            val cardId = "AABBCC11223344"

            val error = Error.UserIdentityNotFound("msg", mapOf("cardId" to cardId))

            whenever(authenticationService.basic(UserPasswordCredential(cardId, password)))
                .thenReturn(UserPrincipal(actingUser))

            whenever(
                removingCardIdentity.removeCardIdentity(
                    eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                    any(),
                    eq(userId),
                    eq(cardId)
                )
            ).thenReturn(error.some())

            // when
            val result = handleRequest(
                HttpMethod.Delete,
                "/api/v1/user/${userId.serialize()}/identity/card/$cardId"
            ) {
                addBasicAuth(cardId, password)
            }

            // then
            assertThat(result.response.status()).isEqualTo(HttpStatusCode.UnprocessableEntity)
            assertThat(result.response.content)
                .isNotNull()
                .isJson<cloud.fabX.fabXaccess.common.rest.Error>()
                .isEqualTo(
                    cloud.fabX.fabXaccess.common.rest.Error(
                        "UserIdentityNotFound",
                        "msg",
                        mapOf("cardId" to cardId)
                    )
                )
        }
}