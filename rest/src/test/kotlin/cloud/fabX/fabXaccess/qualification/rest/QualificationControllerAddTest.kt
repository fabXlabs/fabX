package cloud.fabX.fabXaccess.qualification.rest

import arrow.core.getOrElse
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import cloud.fabX.fabXaccess.RestModule
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.rest.addBasicAuth
import cloud.fabX.fabXaccess.common.rest.isJson
import cloud.fabX.fabXaccess.common.rest.withTestApp
import cloud.fabX.fabXaccess.qualification.application.AddingQualification
import cloud.fabX.fabXaccess.qualification.application.DeletingQualification
import cloud.fabX.fabXaccess.qualification.application.GettingQualification
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFixture
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.rest.AuthenticationService
import cloud.fabX.fabXaccess.user.rest.ErrorPrincipal
import cloud.fabX.fabXaccess.user.rest.UserPrincipal
import io.ktor.auth.UserPasswordCredential
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.util.InternalAPI
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever

@InternalAPI
@ExperimentalSerializationApi
@MockitoSettings
internal class QualificationControllerAddTest {
    private lateinit var addingQualification: AddingQualification
    private lateinit var authenticationService: AuthenticationService

    private val username = "some.one"
    private val password = "supersecret123"

    private val actingUser = UserFixture.arbitrary(isAdmin = true)

    @BeforeEach
    fun `configure RestModule`(
        @Mock gettingQualification: GettingQualification,
        @Mock addingQualification: AddingQualification,
        @Mock deletingQualification: DeletingQualification,
        @Mock authenticationService: AuthenticationService
    ) {
        this.addingQualification = addingQualification
        this.authenticationService = authenticationService

        RestModule.reset()
        RestModule.overrideAuthenticationService(authenticationService)
        RestModule.configureGettingQualification(gettingQualification)
        RestModule.configureAddingQualification(addingQualification)
        RestModule.configureDeletingQualification(deletingQualification)
    }

    @Test
    fun `when adding qualification then returns http ok`() = withTestApp {
        // given
        val name = "qualification1"
        val description = "some qualification"
        val colour = "#aabbcc"
        val orderNr = 42

        val requestBody = QualificationCreationDetails(name, description, colour, orderNr)

        val qualificationId = QualificationIdFixture.arbitrary()

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        whenever(
            addingQualification.addQualification(
                eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                any(),
                eq(name),
                eq(description),
                eq(colour),
                eq(orderNr)
            )
        ).thenReturn(qualificationId.right())

        // when
        val result = handleRequest(HttpMethod.Post, "/api/v1/qualification") {
            addBasicAuth(username, password)
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(requestBody))
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.OK)
        assertThat(result.response.content).isEqualTo(qualificationId.serialize())
    }

    @Test
    fun `given no admin authentication when adding qualification then returns http forbidden`() = withTestApp {
        // given
        val requestBody = QualificationCreationDetails(
            "qualification1",
            "some qualification",
            "#aabbcc",
            42
        )

        val message = "abc123"
        val error = Error.UserNotAdmin(message)

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(ErrorPrincipal(error))

        // when
        val result = handleRequest(HttpMethod.Post, "/api/v1/qualification") {
            addBasicAuth(username, password)
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(requestBody))
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.Forbidden)
        assertThat(result.response.content)
            .isNotNull()
            .isJson<cloud.fabX.fabXaccess.common.rest.Error>()
            .transform { it.message }
            .isEqualTo(message)
    }
}