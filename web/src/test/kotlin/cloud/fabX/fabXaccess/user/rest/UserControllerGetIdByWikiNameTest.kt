package cloud.fabX.fabXaccess.user.rest

import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.rest.c
import cloud.fabX.fabXaccess.common.rest.isError
import cloud.fabX.fabXaccess.common.rest.withTestApp
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFixture
import cloud.fabX.fabXaccess.user.application.GettingUserIdByWikiName
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
internal class UserControllerGetIdByWikiNameTest {
    private lateinit var gettingUserIdByWikiName: GettingUserIdByWikiName
    private lateinit var authenticationService: AuthenticationService

    private val username = "some.one"
    private val password = "some.password"

    private val actingUser = UserFixture.arbitrary(instructorQualifications = setOf(QualificationIdFixture.arbitrary()))

    @BeforeEach
    fun `configure WebModule`(
        @Mock gettingUserIdByWikiName: GettingUserIdByWikiName,
        @Mock authenticationService: AuthenticationService
    ) {
        this.gettingUserIdByWikiName = gettingUserIdByWikiName
        this.authenticationService = authenticationService

        runTest {
            whenever(authenticationService.basic(UserPasswordCredential(username, password)))
                .thenReturn(UserPrincipal(actingUser))
        }
    }

    private fun withConfiguredTestApp(block: suspend ApplicationTestBuilder.() -> Unit) = withTestApp({
        bindInstance(overrides = true) { gettingUserIdByWikiName }
        bindInstance(overrides = true) { authenticationService }
    }, block)

    @Test
    fun `when getting user id by wiki name then returns wiki name`() = withConfiguredTestApp {
        // given
        val userId = UserIdFixture.arbitrary()
        val wikiName = "some.member"

        whenever(
            gettingUserIdByWikiName.getUserIdByWikiName(
                eq(actingUser.asInstructor().getOrElse { throw IllegalStateException() }),
                any(),
                eq(wikiName)
            )
        ).thenReturn(userId.right())

        // when
        val response = c().get("/api/v1/user/id-by-wiki-name") {
            url {
                parameters.append("wikiName", wikiName)
            }
            basicAuth(username, password)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.body<String>()).isEqualTo(userId.serialize())
    }

    @Test
    fun `given no instructor authentication when getting user id by wiki name then returns http forbidden`() =
        withConfiguredTestApp {
            // given
            val message = "msg123"
            val error = Error.UserNotAdmin(message)

            whenever(authenticationService.basic(UserPasswordCredential(username, password)))
                .thenReturn(ErrorPrincipal(error))

            // when
            val response = c().get("/api/v1/user/id-by-wiki-name") {
                url {
                    parameters.append("wikiName", "some.member")
                }
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

    @Test
    fun `given no wiki name when getting user id by wiki name then returns http bad request`() = withConfiguredTestApp {
        // given

        // when
        val response = c().get("/api/v1/user/id-by-wiki-name") {
            // no parameter appended
            basicAuth(username, password)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.BadRequest)
    }

    @Test
    fun `given domain error when getting user id by wiki name then returns mapped error`() = withConfiguredTestApp {
        // given
        val expectedError = Error.UserNotFoundByWikiName("msg")
        val wikiName = "some.member"

        whenever(
            gettingUserIdByWikiName.getUserIdByWikiName(
                eq(actingUser.asInstructor().getOrElse { throw IllegalStateException() }),
                any(),
                eq(wikiName)
            )
        ).thenReturn(expectedError.left())

        // when
        val response = c().get("/api/v1/user/id-by-wiki-name") {
            url {
                parameters.append("wikiName", wikiName)
            }
            basicAuth(username, password)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NotFound)
        assertThat(response.body<cloud.fabX.fabXaccess.common.rest.Error>())
            .isError(
                "UserNotFoundByWikiName",
                "msg"
            )
    }
}