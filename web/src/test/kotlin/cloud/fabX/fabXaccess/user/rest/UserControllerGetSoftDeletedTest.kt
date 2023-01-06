package cloud.fabX.fabXaccess.user.rest

import arrow.core.getOrElse
import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.rest.c
import cloud.fabX.fabXaccess.common.rest.isError
import cloud.fabX.fabXaccess.common.rest.withTestApp
import cloud.fabX.fabXaccess.user.application.GettingSoftDeletedUsers
import cloud.fabX.fabXaccess.user.model.UserFixture
import io.ktor.client.call.body
import io.ktor.client.request.basicAuth
import io.ktor.client.request.get
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
class UserControllerGetSoftDeletedTest {
    private lateinit var gettingSoftDeletedUsers: GettingSoftDeletedUsers
    private lateinit var authenticationService: AuthenticationService

    private val username = "some.one"
    private val password = "some.password"

    private val actingUser = UserFixture.arbitrary(isAdmin = true)

    @BeforeEach
    fun `configure WebModule`(
        @Mock gettingSoftDeletedUsers: GettingSoftDeletedUsers,
        @Mock authenticationService: AuthenticationService
    ) {
        this.gettingSoftDeletedUsers = gettingSoftDeletedUsers
        this.authenticationService = authenticationService

        runTest {
            whenever(authenticationService.basic(UserPasswordCredential(username, password)))
                .thenReturn(UserPrincipal(actingUser))
        }
    }

    private fun withConfiguredTestApp(block: suspend ApplicationTestBuilder.() -> Unit) = withTestApp({
        bindInstance(overrides = true) { gettingSoftDeletedUsers }
        bindInstance(overrides = true) { authenticationService }
    }, block)

    @Test
    fun `when getting soft deleted users then returns users`() = withConfiguredTestApp {
        // given
        val user1 = UserFixture.arbitrary()
        val user2 = UserFixture.arbitrary()

        whenever(
            gettingSoftDeletedUsers.getSoftDeletedUsers(
                eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                any()
            )
        ).thenReturn(setOf(user1, user2))

        // when
        val response = c().get("/api/v1/user/soft-deleted") {
            basicAuth(username, password)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.body<Set<User>>()).isEqualTo(
            setOf(
                user1.toRestModel(),
                user2.toRestModel()
            )
        )
    }

    @Test
    fun `given no admin authentication when getting soft deleted users then returns http forbidden`() = withConfiguredTestApp {
        // given
        val message = "msg123"
        val error = Error.UserNotAdmin(message)

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(ErrorPrincipal(error))

        // when
        val response = c().get("/api/v1/user/soft-deleted") {
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
}