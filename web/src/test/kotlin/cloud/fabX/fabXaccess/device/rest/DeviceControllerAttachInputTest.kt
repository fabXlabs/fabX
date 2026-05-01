package cloud.fabX.fabXaccess.device.rest

import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.rest.c
import cloud.fabX.fabXaccess.common.rest.isError
import cloud.fabX.fabXaccess.common.rest.withTestApp
import cloud.fabX.fabXaccess.device.application.AttachingInput
import cloud.fabX.fabXaccess.device.model.DeviceIdFixture
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.rest.AuthenticationService
import cloud.fabX.fabXaccess.user.rest.ErrorPrincipal
import cloud.fabX.fabXaccess.user.rest.UserPrincipal
import io.ktor.client.call.body
import io.ktor.client.request.basicAuth
import io.ktor.client.request.put
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
internal class DeviceControllerAttachInputTest {
    private lateinit var attachingInput: AttachingInput
    private lateinit var authenticationService: AuthenticationService

    private val username = "some.one"
    private val password = "supersecret123"

    private val actingUser = UserFixture.arbitrary(isAdmin = true)

    @BeforeEach
    fun `configure WebModule`(
        @Mock attachingInput: AttachingInput,
        @Mock authenticationService: AuthenticationService
    ) {
        this.attachingInput = attachingInput
        this.authenticationService = authenticationService
    }

    private fun withConfiguredTestApp(block: suspend ApplicationTestBuilder.() -> Unit) = withTestApp({
        bindInstance(overrides = true) { attachingInput }
        bindInstance(overrides = true) { authenticationService }
    }, block)

    @Test
    fun `when attaching input then returns http no content`() = withConfiguredTestApp {
        // given
        val deviceId = DeviceIdFixture.arbitrary()
        val pin = 4
        val name = "input name"
        val descriptionLow = "description low"
        val descriptionHigh = "description high"
        val colourLow = "#aabbcc"
        val colourHigh = "#ddeeff"

        val requestBody = InputAttachmentDetails(
            name,
            descriptionLow,
            descriptionHigh,
            colourLow,
            colourHigh
        )

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        whenever(
            attachingInput.attachInput(
                eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                any(),
                eq(deviceId),
                eq(pin),
                eq(name),
                eq(descriptionLow),
                eq(descriptionHigh),
                eq(colourLow),
                eq(colourHigh)
            )
        ).thenReturn(Unit.right())

        // when
        val response = c().put("/api/v1/device/${deviceId.serialize()}/attached-input/$pin") {
            basicAuth(username, password)
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NoContent)
        assertThat(response.bodyAsText()).isEmpty()
    }

    @Test
    fun `given no admin authentication when attaching input then returns http forbidden`() = withConfiguredTestApp {
        // given
        val requestBody = InputAttachmentDetails(
            "input name",
            "description low",
            "description high",
            "#aabbcc",
            "#ddeeff"
        )

        val message = "abc1234"
        val error = Error.UserNotAdmin(message)

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(ErrorPrincipal(error))

        // when
        val response = c().put("/api/v1/device/${DeviceIdFixture.arbitrary().serialize()}/attached-input/42") {
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
    fun `given no body when attaching input then returns http unprocessable entity`() = withConfiguredTestApp {
        // given
        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        // when
        val response = c().put("/api/v1/device/${DeviceIdFixture.arbitrary().serialize()}/attached-input/123") {
            basicAuth(username, password)
            contentType(ContentType.Application.Json)
            // empty body
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.BadRequest)
    }

    @Test
    fun `given invalid device id when attaching input then returns http bad request`() = withConfiguredTestApp {
        // given
        val invalidDeviceId = "invalidDeviceId"

        val requestBody = InputAttachmentDetails(
            "input name",
            "description low",
            "description high",
            "#aabbcc",
            "#ddeeff"
        )

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        // when
        val response = c().put("/api/v1/device/$invalidDeviceId/attached-input/123") {
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
    fun `given invalid pin when attaching input then returns http bad request`() = withConfiguredTestApp {
        // given
        val invalidPin = "invalidPin"

        val requestBody = InputAttachmentDetails(
            "input name",
            "description low",
            "description high",
            "#aabbcc",
            "#ddeeff"
        )

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        // when
        val response = c().put("/api/v1/device/${DeviceIdFixture.arbitrary().serialize()}/attached-input/$invalidPin") {
            basicAuth(username, password)
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.BadRequest)
        assertThat(response.body<String>())
            .isEqualTo("Required int parameter \"pin\" not given or invalid.")
    }

    @Test
    fun `given domain error when attaching input then returns mapped domain error`() = withConfiguredTestApp {
        val deviceId = DeviceIdFixture.arbitrary()

        val pin = 4
        val name = "input name"
        val descriptionLow = "description low"
        val descriptionHigh = "description high"
        val colourLow = "#aabbcc"
        val colourHigh = "#ddeeff"

        val requestBody = InputAttachmentDetails(
            name,
            descriptionLow,
            descriptionHigh,
            colourLow,
            colourHigh
        )

        val correlationId = CorrelationIdFixture.arbitrary()

        val error = Error.InputPinInUse(
            "some message",
            pin,
            correlationId
        )

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        whenever(
            attachingInput.attachInput(
                eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                any(),
                eq(deviceId),
                eq(pin),
                eq(name),
                eq(descriptionLow),
                eq(descriptionHigh),
                eq(colourLow),
                eq(colourHigh)
            )
        ).thenReturn(error.left())

        // when
        val response = c().put("/api/v1/device/${deviceId.serialize()}/attached-input/$pin") {
            basicAuth(username, password)
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.UnprocessableEntity)
        assertThat(response.body<cloud.fabX.fabXaccess.common.rest.Error>())
            .isError(
                "InputPinInUse",
                "some message",
                mapOf("pin" to pin.toString()),
                correlationId.serialize()
            )
    }
}
