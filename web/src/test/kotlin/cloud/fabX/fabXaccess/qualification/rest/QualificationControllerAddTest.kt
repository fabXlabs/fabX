package cloud.fabX.fabXaccess.qualification.rest

import arrow.core.getOrElse
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.rest.c
import cloud.fabX.fabXaccess.common.rest.withTestApp
import cloud.fabX.fabXaccess.qualification.application.AddingQualification
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFixture
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.rest.AuthenticationService
import cloud.fabX.fabXaccess.user.rest.ErrorPrincipal
import cloud.fabX.fabXaccess.user.rest.UserPrincipal
import io.ktor.client.call.body
import io.ktor.client.request.basicAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
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
internal class QualificationControllerAddTest {
    private lateinit var addingQualification: AddingQualification
    private lateinit var authenticationService: AuthenticationService

    private val username = "some.one"
    private val password = "supersecret123"

    private val actingUser = UserFixture.arbitrary(isAdmin = true)

    @BeforeEach
    fun `configure WebModule`(
        @Mock addingQualification: AddingQualification,
        @Mock authenticationService: AuthenticationService
    ) {
        this.addingQualification = addingQualification
        this.authenticationService = authenticationService
    }

    private fun withConfiguredTestApp(block: suspend ApplicationTestBuilder.() -> Unit) = withTestApp({
        bindInstance(overrides = true) { addingQualification }
        bindInstance(overrides = true) { authenticationService }
    }, block)

    @Test
    fun `when adding qualification then returns http ok`() = withConfiguredTestApp {
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
        val response = c().post("/api/v1/qualification") {
            basicAuth(username, password)
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.body<String>()).isEqualTo(qualificationId.serialize())
    }

    @Test
    fun `given no admin authentication when adding qualification then returns http forbidden`() =
        withConfiguredTestApp {
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
            val response = c().post("/api/v1/qualification") {
                basicAuth(username, password)
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            // then
            assertThat(response.status).isEqualTo(HttpStatusCode.Forbidden)
            assertThat(response.body<cloud.fabX.fabXaccess.common.rest.Error>())
                .transform { it.message }
                .isEqualTo(message)
        }
}