package cloud.fabX.fabXaccess.device.rest

import arrow.core.None
import arrow.core.getOrElse
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.rest.ChangeableValue
import cloud.fabX.fabXaccess.common.rest.addBasicAuth
import cloud.fabX.fabXaccess.common.rest.isError
import cloud.fabX.fabXaccess.common.rest.withTestApp
import cloud.fabX.fabXaccess.device.application.ChangingDevice
import cloud.fabX.fabXaccess.device.model.DeviceIdFixture
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.rest.AuthenticationService
import cloud.fabX.fabXaccess.user.rest.ErrorPrincipal
import cloud.fabX.fabXaccess.user.rest.UserPrincipal
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.UserPasswordCredential
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
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

@MockitoSettings
internal class DeviceControllerPutTest {
    private lateinit var changingDevice: ChangingDevice
    private lateinit var authenticationService: AuthenticationService

    private val username = "some.one"
    private val password = "supersecret123"

    private val actingUser = UserFixture.arbitrary(isAdmin = true)

    @BeforeEach
    fun `configure WebModule`(
        @Mock changingDevice: ChangingDevice,
        @Mock authenticationService: AuthenticationService
    ) {
        this.changingDevice = changingDevice
        this.authenticationService = authenticationService
    }

    private fun withConfiguredTestApp(block: suspend TestApplicationEngine.() -> Unit) = withTestApp({
        bindInstance(overrides = true) { changingDevice }
        bindInstance(overrides = true) { authenticationService }
    }, block)

    @Test
    fun `when changing device then returns http no content`() = withConfiguredTestApp {
        // given
        val id = DeviceIdFixture.arbitrary()

        val requestBody = DeviceDetails(
            ChangeableValue("newName"),
            null,
            ChangeableValue("https://backup.example.com/new")
        )

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        whenever(
            changingDevice.changeDeviceDetails(
                eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                any(),
                eq(id),
                eq(cloud.fabX.fabXaccess.common.model.ChangeableValue.ChangeToValueString("newName")),
                eq(cloud.fabX.fabXaccess.common.model.ChangeableValue.LeaveAsIs),
                eq(cloud.fabX.fabXaccess.common.model.ChangeableValue.ChangeToValueString("https://backup.example.com/new"))
            )
        ).thenReturn(None)

        // when
        val result = handleRequest(HttpMethod.Put, "/api/v1/device/${id.serialize()}") {
            addBasicAuth(username, password)
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(requestBody))
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.NoContent)
        assertThat(result.response.content).isNull()
    }

    @Test
    fun `given no admin authentication when changing device then returns http forbidden`() = withConfiguredTestApp {
        // given
        val requestBody = DeviceDetails(
            ChangeableValue("newName"),
            null,
            ChangeableValue("https://backup.example.com/new")
        )

        val message = "abc1234"
        val error = Error.UserNotAdmin(message)

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(ErrorPrincipal(error))

        // when
        val result = handleRequest(HttpMethod.Put, "/api/v1/device/${DeviceIdFixture.arbitrary().serialize()}") {
            addBasicAuth(username, password)
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(requestBody))
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.Forbidden)
        assertThat(result.response.content)
            .isError(
                type = "UserNotAdmin",
                message = message
            )
    }

    @Test
    fun `given no body when changing device then returns http unprocessable entity`() = withConfiguredTestApp {
        // given
        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        // when
        val result = handleRequest(HttpMethod.Put, "/api/v1/device/${DeviceIdFixture.arbitrary().serialize()}") {
            addBasicAuth(username, password)
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            // empty body
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.UnprocessableEntity)
    }
}