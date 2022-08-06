package cloud.fabX.fabXaccess.device.rest

import arrow.core.getOrElse
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.rest.c
import cloud.fabX.fabXaccess.common.rest.withTestApp
import cloud.fabX.fabXaccess.device.application.AddingDevice
import cloud.fabX.fabXaccess.device.model.DeviceIdFixture
import cloud.fabX.fabXaccess.device.model.MacSecretIdentity
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.rest.AuthenticationService
import cloud.fabX.fabXaccess.user.rest.ErrorPrincipal
import cloud.fabX.fabXaccess.user.rest.UserPrincipal
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
class DeviceControllerAddTest {
    private lateinit var addingDevice: AddingDevice
    private lateinit var authenticationService: AuthenticationService

    private val username = "some.one"
    private val password = "supersecret123"

    private val actingUser = UserFixture.arbitrary(isAdmin = true)

    @BeforeEach
    fun `configure WebModule`(
        @Mock addingDevice: AddingDevice,
        @Mock authenticationService: AuthenticationService
    ) {
        this.addingDevice = addingDevice
        this.authenticationService = authenticationService
    }

    private fun withConfiguredTestApp(block: suspend ApplicationTestBuilder.() -> Unit) = withTestApp({
        bindInstance(overrides = true) { addingDevice }
        bindInstance(overrides = true) { authenticationService }
    }, block)

    @Test
    fun `when adding device then returns http ok`() = withConfiguredTestApp {
        // given
        val requestBody = DeviceCreationDetails(
            "device1",
            "https://example.com/bg1.bmp",
            "https://backup.example.com",
            "aabbccddeeff",
            "supersecret"
        )

        val deviceId = DeviceIdFixture.arbitrary()

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        whenever(
            addingDevice.addDevice(
                eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                any(),
                eq("device1"),
                eq("https://example.com/bg1.bmp"),
                eq("https://backup.example.com"),
                eq(MacSecretIdentity("aabbccddeeff", "supersecret"))
            )
        ).thenReturn(deviceId.right())

        // when
        val response = c().post("/api/v1/device") {
            basicAuth(username, password)
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.bodyAsText()).isEqualTo(deviceId.serialize())
    }

    @Test
    fun `given no admin authentication when adding device then returns http forbidden`() = withConfiguredTestApp {
        // given
        val requestBody = DeviceCreationDetails(
            "device1",
            "https://example.com/bg1.bmp",
            "https://backup.example.com",
            "aabbccddeeff",
            "supersecret"
        )

        val message = "msg"
        val error = Error.UserNotAdmin(message)

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(ErrorPrincipal(error))

        // when
        val response = c().post("/api/v1/device") {
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