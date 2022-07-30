package cloud.fabX.fabXaccess.device.rest

import arrow.core.None
import arrow.core.getOrElse
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.rest.addBasicAuth
import cloud.fabX.fabXaccess.common.rest.isJson
import cloud.fabX.fabXaccess.common.rest.withTestApp
import cloud.fabX.fabXaccess.device.application.DeletingDevice
import cloud.fabX.fabXaccess.device.model.DeviceIdFixture
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.rest.AuthenticationService
import cloud.fabX.fabXaccess.user.rest.ErrorPrincipal
import cloud.fabX.fabXaccess.user.rest.UserPrincipal
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
internal class DeviceControllerDeleteTest {
    private lateinit var deletingDevice: DeletingDevice
    private lateinit var authenticationService: AuthenticationService

    private val username = "some.one"
    private val password = "supersecret123"

    private val actingUser = UserFixture.arbitrary(isAdmin = true)

    @BeforeEach
    fun `configure RestModule`(
        @Mock deletingDevice: DeletingDevice,
        @Mock authenticationService: AuthenticationService
    ) {
        this.deletingDevice = deletingDevice
        this.authenticationService = authenticationService
    }

    private fun withConfiguredTestApp(block: suspend TestApplicationEngine.() -> Unit) = withTestApp({
        bindInstance(overrides = true) { deletingDevice }
        bindInstance(overrides = true) { authenticationService }
    }, block)

    @Test
    fun `when delete device then returns http no content`() = withConfiguredTestApp {
        // given
        val deviceId = DeviceIdFixture.arbitrary()

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        whenever(
            deletingDevice.deleteDevice(
                eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                any(),
                eq(deviceId)
            )
        ).thenReturn(None)

        // when
        val result = handleRequest(HttpMethod.Delete, "/api/v1/device/${deviceId.serialize()}") {
            addBasicAuth(username, password)
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.NoContent)
        assertThat(result.response.content).isNull()
    }

    @Test
    fun `given no admin authentication when deleting tool then returns http forbidden`() = withConfiguredTestApp {
        // given
        val deviceId = DeviceIdFixture.arbitrary()

        val message = "errormessage"
        val error = Error.UserNotAdmin(message)

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(ErrorPrincipal(error))


        // when
        val result = handleRequest(HttpMethod.Delete, "/api/v1/device/${deviceId.serialize()}") {
            addBasicAuth(username, password)
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