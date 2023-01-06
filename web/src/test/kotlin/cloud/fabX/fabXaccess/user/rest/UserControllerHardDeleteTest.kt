package cloud.fabX.fabXaccess.user.rest

import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.rest.Error
import cloud.fabX.fabXaccess.common.rest.c
import cloud.fabX.fabXaccess.common.rest.isError
import cloud.fabX.fabXaccess.common.rest.withTestApp
import cloud.fabX.fabXaccess.user.application.DeletingUser
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import io.ktor.client.call.body
import io.ktor.client.request.basicAuth
import io.ktor.client.request.delete
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.UserPasswordCredential
import io.ktor.server.testing.ApplicationTestBuilder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.kodein.di.bindInstance
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@MockitoSettings
class UserControllerHardDeleteTest {
    private lateinit var deletingUser: DeletingUser
    private lateinit var authenticationService: AuthenticationService

    private val username = "some.one"
    private val password = "some.password"

    private val actingUser = UserFixture.arbitrary(isAdmin = true)

    @BeforeEach
    fun `configure WebModule`(
        @Mock deletingUser: DeletingUser,
        @Mock authenticationService: AuthenticationService
    ) {
        this.deletingUser = deletingUser
        this.authenticationService = authenticationService

        runTest {
            whenever(authenticationService.basic(UserPasswordCredential(username, password)))
                .thenReturn(UserPrincipal(actingUser))
        }
    }

    private fun withConfiguredTestApp(block: suspend ApplicationTestBuilder.() -> Unit) = withTestApp({
        bindInstance(overrides = true) { deletingUser }
        bindInstance(overrides = true) { authenticationService }
    }, block)

    @Test
    fun `when hard deleting user then returns http no content`() = withConfiguredTestApp {
        // given
        val userId = UserIdFixture.arbitrary()

        whenever(
            deletingUser.hardDeleteUser(
                eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                any(),
                eq(userId)
            )
        ).thenReturn(Unit.right())

        // when
        val response = c().delete("/api/v1/user/soft-deleted/${userId.serialize()}") {
            basicAuth(username, password)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NoContent)
        assertThat(response.bodyAsText()).isEmpty()
    }

    @Test
    fun `given domain error when hard deleting user then returns mapped error`() = withConfiguredTestApp {
        // given
        val userId = UserIdFixture.arbitrary()
        val expectedError = cloud.fabX.fabXaccess.common.model.Error.UserNotFound("msg", userId)

        whenever(
            deletingUser.hardDeleteUser(
                eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                any(),
                eq(userId)
            )
        ).thenReturn(expectedError.left())

        // when
        val response = c().delete("/api/v1/user/soft-deleted/${userId.serialize()}") {
            basicAuth(username, password)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NotFound)
        assertThat(response.body<Error>())
            .isEqualTo(Error(
                "UserNotFound",
                "msg",
                mapOf("userId" to userId.serialize()),
                null
            ))
    }

    @Test
    fun `given no admin authentication when hard deleting user then returns http forbidden`() = withConfiguredTestApp {
        // given
        val message = "msg123"
        val error = cloud.fabX.fabXaccess.common.model.Error.UserNotAdmin(message)

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(ErrorPrincipal(error))

        // when
        val response = c().delete("/api/v1/user/soft-deleted/${UserIdFixture.arbitrary().serialize()}") {
            basicAuth(username, password)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.Forbidden)
        assertThat(response.body<Error>())
            .isError(
                "UserNotAdmin",
                message
            )
    }
}