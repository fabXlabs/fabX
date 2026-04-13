package cloud.fabX.fabXaccess.device.rest

import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsOnly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.rest.c
import cloud.fabX.fabXaccess.common.rest.isError
import cloud.fabX.fabXaccess.common.rest.withTestApp
import cloud.fabX.fabXaccess.device.application.GettingDeviceConnectionStatus
import cloud.fabX.fabXaccess.device.model.DeviceIdFixture
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.rest.AuthenticationService
import cloud.fabX.fabXaccess.user.rest.UserPrincipal
import io.ktor.client.call.body
import io.ktor.client.request.basicAuth
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.UserPasswordCredential
import io.ktor.server.testing.ApplicationTestBuilder
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.kodein.di.bindInstance
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever

@MockitoSettings
internal class DeviceControllerConnectionStatusTest {
    private lateinit var gettingDeviceConnectionStatus: GettingDeviceConnectionStatus
    private lateinit var authenticationService: AuthenticationService

    private val username = "some.one"
    private val password = "supersecret123"

    private val actingUser = UserFixture.arbitrary(isAdmin = true)

    @BeforeEach
    fun `configure WebModule`(
        @Mock gettingDeviceConnectionStatus: GettingDeviceConnectionStatus,
        @Mock authenticationService: AuthenticationService
    ) {
        this.gettingDeviceConnectionStatus = gettingDeviceConnectionStatus
        this.authenticationService = authenticationService

        runTest {
            whenever(authenticationService.basic(UserPasswordCredential(username, password)))
                .thenReturn(UserPrincipal(actingUser))
        }
    }

    private fun withConfiguredTestApp(block: suspend ApplicationTestBuilder.() -> Unit) = withTestApp({
        bindInstance(overrides = true) { gettingDeviceConnectionStatus }
        bindInstance(overrides = true) { authenticationService }
    }, block)

    @Test
    fun `given no device status when get device connection statuses then returns empty map`() = withConfiguredTestApp {
        // given
        whenever(
            gettingDeviceConnectionStatus.getAll(
                eq(actingUser.asMember()),
                any()
            )
        )
            .thenReturn(emptyMap())

        // when
        val response = c().get("/api/v1/device/connection-status") {
            basicAuth(username, password)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.body<Map<String, Boolean>>())
            .isEmpty()
    }

    @Test
    fun `when get device connection statuses then returns status map`() = withConfiguredTestApp {
        // given
        val deviceId1 = DeviceIdFixture.arbitrary()
        val deviceId2 = DeviceIdFixture.arbitrary()
        val deviceId3 = DeviceIdFixture.arbitrary()

        whenever(
            gettingDeviceConnectionStatus.getAll(
                eq(actingUser.asMember()),
                any()
            )
        )
            .thenReturn(
                mapOf(
                    deviceId1 to true,
                    deviceId2 to false,
                    deviceId3 to true
                )
            )

        // when
        val response = c().get("/api/v1/device/connection-status") {
            basicAuth(username, password)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.body<Map<String, Boolean>>())
            .containsOnly(
                deviceId1.serialize() to true,
                deviceId2.serialize() to false,
                deviceId3.serialize() to true
            )
    }

    @Test
    fun `when get device connection status by id then returns status`() = withConfiguredTestApp {
        // given
        val deviceId = DeviceIdFixture.arbitrary()

        whenever(
            gettingDeviceConnectionStatus.getById(
                eq(actingUser.asMember()),
                any(),
                eq(deviceId)
            )
        )
            .thenReturn(true.right())

        // when
        val response = c().get("/api/v1/device/${deviceId.serialize()}/connection-status") {
            basicAuth(username, password)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.body<Boolean>())
            .isEqualTo(true)
    }

    @Test
    fun `given device not found when get device connection status by id then returns mapped error`() = withConfiguredTestApp {
        // given
        val deviceId = DeviceIdFixture.arbitrary()
        val error = Error.DeviceNotFound("msg", deviceId)

        whenever(
            gettingDeviceConnectionStatus.getById(
                eq(actingUser.asMember()),
                any(),
                eq(deviceId)
            )
        ).thenReturn(error.left())

        // when
        val response = c().get("/api/v1/device/${deviceId.serialize()}/connection-status") {
            basicAuth(username, password)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NotFound)
        assertThat(response.body<cloud.fabX.fabXaccess.common.rest.Error>())
            .isError(
                "DeviceNotFound",
                "msg",
                mapOf("deviceId" to deviceId.serialize())
            )
    }
}
