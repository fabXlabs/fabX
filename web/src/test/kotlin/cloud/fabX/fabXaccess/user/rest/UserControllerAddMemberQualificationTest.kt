package cloud.fabX.fabXaccess.user.rest

import arrow.core.None
import arrow.core.getOrElse
import arrow.core.some
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.rest.c
import cloud.fabX.fabXaccess.common.rest.isError
import cloud.fabX.fabXaccess.common.rest.withTestApp
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFixture
import cloud.fabX.fabXaccess.user.application.AddingMemberQualification
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import io.ktor.client.call.body
import io.ktor.client.request.basicAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.auth.UserPasswordCredential
import io.ktor.server.testing.ApplicationTestBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.kodein.di.bindInstance
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever

@MockitoSettings
internal class UserControllerAddMemberQualificationTest {
    private lateinit var addingMemberQualification: AddingMemberQualification
    private lateinit var authenticationService: AuthenticationService

    private val username = "some.one"
    private val password = "supersecret123"

    private val qualificationId = QualificationIdFixture.arbitrary()
    private val actingUser = UserFixture.arbitrary(instructorQualifications = setOf(qualificationId))

    @BeforeEach
    fun `configure WebModule`(
        @Mock addingMemberQualification: AddingMemberQualification,
        @Mock authenticationService: AuthenticationService
    ) {
        this.addingMemberQualification = addingMemberQualification
        this.authenticationService = authenticationService
    }

    private fun withConfiguredTestApp(block: suspend ApplicationTestBuilder.() -> Unit) = withTestApp({
        bindInstance(overrides = true) { addingMemberQualification }
        bindInstance(overrides = true) { authenticationService }
    }, block)

    @Test
    fun `when adding member qualification then returns http no content`() = withConfiguredTestApp {
        // given
        val userId = UserIdFixture.arbitrary()
        val requestBody = QualificationAdditionDetails(qualificationId.serialize())

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        whenever(
            addingMemberQualification.addMemberQualification(
                eq(actingUser.asInstructor().getOrElse { throw IllegalStateException() }),
                any(),
                eq(userId),
                eq(qualificationId)
            )
        ).thenReturn(None)

        // when
        val response = c().post("/api/v1/user/${userId.serialize()}/member-qualification") {
            basicAuth(username, password)
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NoContent)
        assertThat(response.bodyAsText()).isEmpty()
    }

    @Test
    fun `given no instructor authentication when adding member qualification then returns http forbidden`() =
        withConfiguredTestApp {
            // given
            val requestBody = QualificationAdditionDetails(qualificationId.serialize())

            val message = "msg123"
            val error = Error.UserNotAdmin(message)

            whenever(authenticationService.basic(UserPasswordCredential(username, password)))
                .thenReturn(ErrorPrincipal(error))

            // when
            val response = c().post("/api/v1/user/${UserIdFixture.arbitrary().serialize()}/member-qualification") {
                basicAuth(username, password)
                contentType(ContentType.Application.Json)
                setBody(requestBody)
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
    fun `given no body when adding member qualification then returns http unprocessable entity`() =
        withConfiguredTestApp {
            // given
            whenever(authenticationService.basic(UserPasswordCredential(username, password)))
                .thenReturn(UserPrincipal(actingUser))

            // when
            val response = c().post("/api/v1/user/${UserIdFixture.arbitrary().serialize()}/member-qualification") {
                basicAuth(username, password)
                contentType(ContentType.Application.Json)
                // no body
            }

            // then
            assertThat(response.status).isEqualTo(HttpStatusCode.BadRequest)
        }

    @Test
    fun `given invalid user id when adding member qualification then returns http bad request`() =
        withConfiguredTestApp {
            // given
            val invalidUserId = "invalidUserId"

            val requestBody = QualificationAdditionDetails(qualificationId.serialize())

            whenever(authenticationService.basic(UserPasswordCredential(username, password)))
                .thenReturn(UserPrincipal(actingUser))

            // when
            val response = c().post("/api/v1/user/$invalidUserId/member-qualification") {
                basicAuth(username, password)
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            // then
            assertThat(response.status).isEqualTo(HttpStatusCode.BadRequest)
            assertThat(response.body<String>())
                .isEqualTo("Required UUID parameter \"id\" not given or invalid.")
        }

    @Test
    fun `given domain error when adding member qualification then returns mapped error`() = withConfiguredTestApp {
        // given
        val userId = UserIdFixture.arbitrary()
        val requestBody = QualificationAdditionDetails(qualificationId.serialize())

        val correlationId = CorrelationIdFixture.arbitrary()
        val error = Error.MemberQualificationAlreadyFound("msg123", qualificationId, correlationId)

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        whenever(
            addingMemberQualification.addMemberQualification(
                eq(actingUser.asInstructor().getOrElse { throw IllegalStateException() }),
                any(),
                eq(userId),
                eq(qualificationId)
            )
        ).thenReturn(error.some())

        // when
        val response = c().post("/api/v1/user/${userId.serialize()}/member-qualification") {
            basicAuth(username, password)
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.UnprocessableEntity)
        assertThat(response.body<cloud.fabX.fabXaccess.common.rest.Error>())
            .isError(
                "MemberQualificationAlreadyFound",
                "msg123",
                mapOf("qualificationId" to qualificationId.serialize()),
                correlationId.serialize()
            )
    }
}