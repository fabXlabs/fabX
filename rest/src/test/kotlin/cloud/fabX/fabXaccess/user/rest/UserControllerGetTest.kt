package cloud.fabX.fabXaccess.user.rest

import arrow.core.getOrElse
import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import cloud.fabX.fabXaccess.RestModule
import cloud.fabX.fabXaccess.common.rest.addBasicAuth
import cloud.fabX.fabXaccess.common.rest.isJson
import cloud.fabX.fabXaccess.common.rest.mockAll
import cloud.fabX.fabXaccess.common.rest.withTestApp
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFixture
import cloud.fabX.fabXaccess.user.application.GettingUser
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import cloud.fabX.fabXaccess.user.model.UsernamePasswordIdentity
import io.ktor.auth.UserPasswordCredential
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.util.InternalAPI
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@InternalAPI
@ExperimentalSerializationApi
@MockitoSettings
class UserControllerGetTest {
    private lateinit var gettingUser: GettingUser
    private lateinit var authenticationService: AuthenticationService

    private val username = "some.one"
    private val password = "some.password"

    private val actingUser = UserFixture.arbitrary(isAdmin = true)

    @BeforeEach
    fun `configure RestModule`(
        @Mock gettingUser: GettingUser,
        @Mock authenticationService: AuthenticationService
    ) {
        this.gettingUser = gettingUser
        this.authenticationService = authenticationService

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        mockAll()
        RestModule.overrideAuthenticationService(authenticationService)
        RestModule.configureGettingUser(gettingUser)
    }

    @Test
    fun `given no users when get users then returns empty set`() = withTestApp {
        // given
        whenever(
            gettingUser.getAll(
                eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                any()
            )
        ).thenReturn(setOf())

        // when
        val result = handleRequest(HttpMethod.Get, "/api/v1/user") {
            addBasicAuth(username, password)
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.OK)
        assertThat(result.response.content)
            .isNotNull()
            .isJson<Set<User>>()
            .isEqualTo(setOf())
    }

    @Test
    fun `when get users then returns mapped users`() = withTestApp {
        // given
        val userId1 = UserIdFixture.arbitrary()
        val user1 = UserFixture.arbitrary(
            userId1,
            34,
            "first1",
            "last1",
            "wiki1",
            false,
            "notes1",
            setOf(UsernamePasswordIdentity("username1", "hash1")),
            setOf(QualificationIdFixture.arbitrary()),
            null,
            false
        )

        val userId2 = UserIdFixture.arbitrary()
        val user2 = UserFixture.arbitrary(
            userId2,
            87,
            "first2",
            "last2",
            "wiki2",
            true,
            null,
            setOf(UsernamePasswordIdentity("username2", "hash2")),
            setOf(QualificationIdFixture.arbitrary(), QualificationIdFixture.arbitrary()),
            setOf(QualificationIdFixture.arbitrary()),
            true
        )

        whenever(
            gettingUser.getAll(
                eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                any()
            )
        ).thenReturn(setOf(user1, user2))

        val mappedUser1 = User(
            userId1.serialize(),
            34,
            "first1",
            "last1",
            "wiki1",
            false,
            "notes1"
        )

        val mappedUser2 = User(
            userId2.serialize(),
            87,
            "first2",
            "last2",
            "wiki2",
            true,
            null
        )

        // when
        val result = handleRequest(HttpMethod.Get, "/api/v1/user") {
            addBasicAuth(username, password)
        }

        // then
        verify(gettingUser).getAll(
            eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
            any()
        )

        assertThat(result.response.status()).isEqualTo(HttpStatusCode.OK)
        assertThat(result.response.content)
            .isNotNull()
            .isJson<Set<User>>()
            .containsExactlyInAnyOrder(mappedUser1, mappedUser2)
    }
}