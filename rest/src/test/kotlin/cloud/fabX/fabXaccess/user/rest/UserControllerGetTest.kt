package cloud.fabX.fabXaccess.user.rest

import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.rest.addBasicAuth
import cloud.fabX.fabXaccess.common.rest.isJson
import cloud.fabX.fabXaccess.common.rest.withTestApp
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFixture
import cloud.fabX.fabXaccess.user.application.GettingUser
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import cloud.fabX.fabXaccess.user.model.UsernamePasswordIdentity
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
    }

    private fun withConfiguredTestApp(block: TestApplicationEngine.() -> Unit) = withTestApp({
        bindInstance(overrides = true) { gettingUser }
        bindInstance(overrides = true) { authenticationService }
    }, block)

    @Test
    fun `given no users when get users then returns empty set`() = withConfiguredTestApp {
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
    fun `when get users then returns mapped users`() = withConfiguredTestApp {
        // given
        val userId1 = UserIdFixture.arbitrary()
        val qualification11 = QualificationIdFixture.arbitrary()
        val user1 = UserFixture.arbitrary(
            userId1,
            34,
            "first1",
            "last1",
            "wiki1",
            false,
            "notes1",
            setOf(UsernamePasswordIdentity("username1", "hash1")),
            setOf(qualification11),
            null,
            false
        )

        val userId2 = UserIdFixture.arbitrary()
        val qualification21 = QualificationIdFixture.arbitrary()
        val qualification22 = QualificationIdFixture.arbitrary()
        val qualification23 = QualificationIdFixture.arbitrary()
        val user2 = UserFixture.arbitrary(
            userId2,
            87,
            "first2",
            "last2",
            "wiki2",
            true,
            null,
            setOf(UsernamePasswordIdentity("username2", "hash2")),
            setOf(qualification21, qualification22),
            setOf(qualification23),
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
            "notes1",
            setOf(qualification11.serialize()),
            null,
            false
        )

        val mappedUser2 = User(
            userId2.serialize(),
            87,
            "first2",
            "last2",
            "wiki2",
            true,
            null,
            setOf(qualification21.serialize(), qualification22.serialize()),
            setOf(qualification23.serialize()),
            true
        )

        // when
        val result = handleRequest(HttpMethod.Get, "/api/v1/user") {
            addBasicAuth(username, password)
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.OK)
        assertThat(result.response.content)
            .isNotNull()
            .isJson<Set<User>>()
            .containsExactlyInAnyOrder(mappedUser1, mappedUser2)
    }

    @Test
    fun `given user exists when get user by id then returns mapped user`() = withConfiguredTestApp {
        // given
        val userId = UserIdFixture.arbitrary()
        val qualificationId = QualificationIdFixture.arbitrary()
        val user = UserFixture.arbitrary(
            userId,
            34,
            "first1",
            "last1",
            "wiki1",
            false,
            "notes1",
            setOf(UsernamePasswordIdentity("username1", "hash1")),
            setOf(qualificationId),
            null,
            false
        )

        val mappedUser = User(
            userId.serialize(),
            34,
            "first1",
            "last1",
            "wiki1",
            false,
            "notes1",
            setOf(qualificationId.serialize()),
            null,
            false
        )

        whenever(
            gettingUser.getById(
                eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                any(),
                eq(userId)
            )
        ).thenReturn(user.right())

        // when
        val result = handleRequest(HttpMethod.Get, "/api/v1/user/${userId.serialize()}") {
            addBasicAuth(username, password)
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.OK)
        assertThat(result.response.content)
            .isNotNull()
            .isJson<User>()
            .isEqualTo(mappedUser)
    }

    @Test
    fun `given user not found when get user by id then returns mapped error`() = withConfiguredTestApp {
        // given
        val userId = UserIdFixture.arbitrary()
        val error = Error.UserNotFound("msg", userId)

        whenever(
            gettingUser.getById(
                eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                any(),
                eq(userId)
            )
        ).thenReturn(error.left())

        // when
        val result = handleRequest(HttpMethod.Get, "/api/v1/user/${userId.serialize()}") {
            addBasicAuth(username, password)
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.NotFound)
        assertThat(result.response.content)
            .isNotNull()
            .isJson<cloud.fabX.fabXaccess.common.rest.Error>()
            .isEqualTo(
                cloud.fabX.fabXaccess.common.rest.Error(
                    "UserNotFound",
                    "msg",
                    mapOf("userId" to userId.serialize())
                )
            )
    }
}