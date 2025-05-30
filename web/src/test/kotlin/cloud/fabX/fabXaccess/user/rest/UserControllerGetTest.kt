package cloud.fabX.fabXaccess.user.rest

import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.ErrorFixture
import cloud.fabX.fabXaccess.common.rest.c
import cloud.fabX.fabXaccess.common.rest.isError
import cloud.fabX.fabXaccess.common.rest.withTestApp
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFixture
import cloud.fabX.fabXaccess.user.application.GettingUser
import cloud.fabX.fabXaccess.user.model.InstructorFixture
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import io.ktor.client.call.body
import io.ktor.client.request.basicAuth
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.UserPasswordCredential
import io.ktor.server.testing.ApplicationTestBuilder
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.kodein.di.bindInstance
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever

@MockitoSettings
class UserControllerGetTest {
    private lateinit var gettingUser: GettingUser
    private lateinit var authenticationService: AuthenticationService

    private val username = "some.one"
    private val password = "some.password"

    private val actingUser = UserFixture.arbitrary(isAdmin = true, instructorQualifications = setOf(QualificationIdFixture.arbitrary())
    )

    @BeforeEach
    fun `configure WebModule`(
        @Mock gettingUser: GettingUser,
        @Mock authenticationService: AuthenticationService
    ) {
        this.gettingUser = gettingUser
        this.authenticationService = authenticationService

        runTest {
            whenever(authenticationService.basic(UserPasswordCredential(username, password)))
                .thenReturn(UserPrincipal(actingUser))
        }
    }

    private fun withConfiguredTestApp(block: suspend ApplicationTestBuilder.() -> Unit) = withTestApp({
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
        val response = c().get("/api/v1/user") {
            basicAuth(username, password)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.body<Set<User>>()).isEqualTo(setOf())
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
            setOf(
                cloud.fabX.fabXaccess.user.model.UsernamePasswordIdentity("username1", "hash1"),
                cloud.fabX.fabXaccess.user.model.PhoneNrIdentity("+49123456789"),
                cloud.fabX.fabXaccess.user.model.CardIdentity(
                    "11223344556677",
                    "EE334F5E740985180C9EDAA6B5A9EB159CFB4F19427C68336D6D23D5015547CE"
                )
            ),
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
            setOf(cloud.fabX.fabXaccess.user.model.UsernamePasswordIdentity("username2", "hash2")),
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
            setOf(
                UsernamePasswordIdentity("username1"),
                PhoneNrIdentity("+49123456789"),
                CardIdentity(
                    "11223344556677",
                    "EE334F5E740985180C9EDAA6B5A9EB159CFB4F19427C68336D6D23D5015547CE"
                )
            ),
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
            setOf(UsernamePasswordIdentity("username2")),
            setOf(qualification21.serialize(), qualification22.serialize()),
            setOf(qualification23.serialize()),
            true
        )

        // when
        val response = c().get("/api/v1/user") {
            basicAuth(username, password)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.body<Set<User>>()).containsExactlyInAnyOrder(mappedUser1, mappedUser2)
    }

    @Test
    fun `given users exists when get user limited returns mapped limited users`() = withConfiguredTestApp {
        // given
        val limitedUser1 = cloud.fabX.fabXaccess.user.model.LimitedUser(
            UserIdFixture.arbitrary(),
            "First1",
            "wiki1"
        )

        val limitedUser2 = cloud.fabX.fabXaccess.user.model.LimitedUser(
            UserIdFixture.arbitrary(),
            "First2",
            "wiki2"
        )

        whenever(
            gettingUser.getAllLimited(
                eq(actingUser.asInstructor().getOrElse { throw IllegalStateException() }),
                any()
            )
        ).thenReturn(setOf(limitedUser1, limitedUser2))

        val mappedLimitedUser1 = cloud.fabX.fabXaccess.user.rest.LimitedUser(
            limitedUser1.id.serialize(),
            limitedUser1.firstName,
            limitedUser1.wikiName
        )

        val mappedLimitedUser2 = cloud.fabX.fabXaccess.user.rest.LimitedUser(
            limitedUser2.id.serialize(),
            limitedUser2.firstName,
            limitedUser2.wikiName
        )

        // when
        val response = c().get("/api/v1/user/limited") {
            basicAuth(username, password)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.body<Set<LimitedUser>>()).containsExactlyInAnyOrder(mappedLimitedUser1, mappedLimitedUser2)
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
            setOf(cloud.fabX.fabXaccess.user.model.UsernamePasswordIdentity("username1", "hash1")),
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
            setOf(UsernamePasswordIdentity("username1")),
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
        val response = c().get("/api/v1/user/${userId.serialize()}") {
            basicAuth(username, password)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.body<User>()).isEqualTo(mappedUser)
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
        val response = c().get("/api/v1/user/${userId.serialize()}") {
            basicAuth(username, password)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NotFound)
        assertThat(response.body<cloud.fabX.fabXaccess.common.rest.Error>())
            .isError(
                "UserNotFound",
                "msg",
                mapOf("userId" to userId.serialize())
            )
    }

    @Test
    fun `when get me then returns mapped user`() = withConfiguredTestApp {
        // given
        val user = UserFixture.arbitrary(
            actingUser.id,
            123,
            "some",
            "one",
            "some.one",
            false,
            null,
            setOf(cloud.fabX.fabXaccess.user.model.UsernamePasswordIdentity("username1", "hash1")),
            setOf(),
            null,
            false
        )

        val mappedUser = User(
            actingUser.id.serialize(),
            123,
            "some",
            "one",
            "some.one",
            false,
            null,
            setOf(UsernamePasswordIdentity("username1")),
            setOf(),
            null,
            false
        )

        whenever(
            gettingUser.getMe(
                eq(actingUser.asMember()),
                any(),
            )
        ).thenReturn(user.right())

        // when
        val response = c().get("/api/v1/user/me") {
            basicAuth(username, password)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.body<User>()).isEqualTo(mappedUser)
    }

    @Test
    fun `given error when get me then returns mapped error`() = withConfiguredTestApp {
        // given
        val error = ErrorFixture.arbitrary()

        whenever(
            gettingUser.getMe(
                eq(actingUser.asMember()),
                any()
            )
        ).thenReturn(error.left())

        // when
        val response = c().get("/api/v1/user/me") {
            basicAuth(username, password)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.UnprocessableEntity)
        assertThat(response.body<cloud.fabX.fabXaccess.common.rest.Error>())
            .isError(
                "VersionConflict",
                "some message",
                mapOf()
            )
    }
}