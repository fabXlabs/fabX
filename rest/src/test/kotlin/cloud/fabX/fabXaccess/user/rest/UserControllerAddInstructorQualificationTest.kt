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
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFixture
import cloud.fabX.fabXaccess.user.application.AddingInstructorQualification
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
internal class UserControllerAddInstructorQualificationTest {
    private lateinit var addingInstructorQualification: AddingInstructorQualification
    private lateinit var authenticationService: AuthenticationService

    private val username = "some.one"
    private val password = "supersecret123"

    private val actingUser = UserFixture.arbitrary(isAdmin = true)

    @BeforeEach
    fun `configure RestModule`(
        @Mock addingInstructorQualification: AddingInstructorQualification,
        @Mock authenticationService: AuthenticationService
    ) {
        this.addingInstructorQualification = addingInstructorQualification
        this.authenticationService = authenticationService
    }

    private fun withConfiguredTestApp(block: TestApplicationEngine.() -> Unit) = withTestApp({
        bindInstance(overrides = true) { addingInstructorQualification }
        bindInstance(overrides = true) { authenticationService }
    }, block)

    @Test
    fun `when adding instructor qualification then returns http ok`() = withConfiguredTestApp {
        // given
        val userId = UserIdFixture.arbitrary()
        val qualificationId = QualificationIdFixture.arbitrary()
        val requestBody = QualificationAdditionDetails(qualificationId.serialize())

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        whenever(
            addingInstructorQualification.addInstructorQualification(
                eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                any(),
                eq(userId),
                eq(qualificationId)
            )
        ).thenReturn(None)

        // when
        val result = handleRequest(HttpMethod.Post, "/api/v1/user/${userId.serialize()}/instructor-qualification") {
            addBasicAuth(username, password)
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(requestBody))
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.OK)
        assertThat(result.response.content).isNull()
    }

    @Test
    fun `given no admin authentication when adding instructor qualification then returns http ok`() =
        withConfiguredTestApp {
            // given
            val requestBody = QualificationAdditionDetails(QualificationIdFixture.arbitrary().serialize())

            val message = "msg123"
            val error = Error.UserNotAdmin(message)

            whenever(authenticationService.basic(UserPasswordCredential(username, password)))
                .thenReturn(ErrorPrincipal(error))

            // when
            val result = handleRequest(
                HttpMethod.Post,
                "/api/v1/user/${UserIdFixture.arbitrary().serialize()}/instructor-qualification"
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
    fun `given no body when adding instructor qualification then returns http unprocessable entity`() =
        withConfiguredTestApp {
            // given
            whenever(authenticationService.basic(UserPasswordCredential(username, password)))
                .thenReturn(UserPrincipal(actingUser))

            // when
            val result = handleRequest(
                HttpMethod.Post,
                "/api/v1/user/${UserIdFixture.arbitrary().serialize()}/instructor-qualification"
            ) {
                addBasicAuth(username, password)
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                // empty body
            }

            // then
            assertThat(result.response.status()).isEqualTo(HttpStatusCode.UnprocessableEntity)
        }

    @Test
    fun `given domain error when adding instructor qualification then returns http ok`() = withConfiguredTestApp {
        // given
        val userId = UserIdFixture.arbitrary()
        val qualificationId = QualificationIdFixture.arbitrary()
        val requestBody = QualificationAdditionDetails(qualificationId.serialize())

        val error = Error.InstructorQualificationAlreadyFound(
            "some message",
            qualificationId
        )

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        whenever(
            addingInstructorQualification.addInstructorQualification(
                eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                any(),
                eq(userId),
                eq(qualificationId)
            )
        ).thenReturn(error.some())

        // when
        val result = handleRequest(HttpMethod.Post, "/api/v1/user/${userId.serialize()}/instructor-qualification") {
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
                    "InstructorQualificationAlreadyFound",
                    "some message",
                    mapOf("qualificationId" to qualificationId.serialize())
                )
            )
    }
}