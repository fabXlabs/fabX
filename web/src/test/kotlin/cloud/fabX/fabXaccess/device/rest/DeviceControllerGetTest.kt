package cloud.fabX.fabXaccess.device.rest

import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.rest.addBasicAuth
import cloud.fabX.fabXaccess.common.rest.isError
import cloud.fabX.fabXaccess.common.rest.isJson
import cloud.fabX.fabXaccess.common.rest.withTestApp
import cloud.fabX.fabXaccess.device.application.GettingDevice
import cloud.fabX.fabXaccess.device.model.DeviceFixture
import cloud.fabX.fabXaccess.device.model.DeviceIdFixture
import cloud.fabX.fabXaccess.tool.model.ToolIdFixture
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.rest.AuthenticationService
import cloud.fabX.fabXaccess.user.rest.UserPrincipal
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.UserPasswordCredential
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.kodein.di.bindInstance
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@MockitoSettings
internal class DeviceControllerGetTest {
    private lateinit var gettingDevice: GettingDevice
    private lateinit var authenticationService: AuthenticationService

    private val username = "some.one"
    private val password = "supersecret123"

    private val actingUser = UserFixture.arbitrary(isAdmin = true)

    @BeforeEach
    fun `configure WebModule`(
        @Mock gettingDevice: GettingDevice,
        @Mock authenticationService: AuthenticationService
    ) {
        this.gettingDevice = gettingDevice
        this.authenticationService = authenticationService

        runTest {
            whenever(authenticationService.basic(UserPasswordCredential(username, password)))
                .thenReturn(UserPrincipal(actingUser))
        }
    }

    private fun withConfiguredTestApp(block: suspend TestApplicationEngine.() -> Unit) = withTestApp({
        bindInstance(overrides = true) { gettingDevice }
        bindInstance(overrides = true) { authenticationService }
    }, block)

    @Test
    fun `given no device when get devices then returns empty set`() = withConfiguredTestApp {
        // given
        whenever(
            gettingDevice.getAll(
                eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                any()
            )
        )
            .thenReturn(setOf())

        // when
        val result = handleRequest(HttpMethod.Get, "/api/v1/device") {
            addBasicAuth(username, password)
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.OK)
        assertThat(result.response.content)
            .isNotNull()
            .isJson<Set<Device>>()
            .isEqualTo(setOf())
    }

    @Test
    fun `when get devices then returns mapped devices`() = withConfiguredTestApp {
        // given
        val toolId1 = ToolIdFixture.arbitrary()
        val toolId2 = ToolIdFixture.arbitrary()

        val deviceId1 = DeviceIdFixture.arbitrary()
        val device1 = DeviceFixture.arbitrary(
            deviceId1,
            345,
            "device1",
            "https://example.com/device1",
            "https://backup.example.com",
            "aabbccddee01",
            "secret1",
            mapOf(1 to toolId1)
        )

        val deviceId2 = DeviceIdFixture.arbitrary()
        val device2 = DeviceFixture.arbitrary(
            deviceId2,
            678,
            "device2",
            "https://example.com/device2",
            "https://backup.example.com",
            "aabbccddee02",
            "secret2",
            mapOf(1 to toolId1, 2 to toolId2)
        )

        whenever(
            gettingDevice.getAll(
                eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                any()
            )
        )
            .thenReturn(setOf(device1, device2))

        val mappedDevice1 = Device(
            deviceId1.serialize(),
            345,
            "device1",
            "https://example.com/device1",
            "https://backup.example.com",
            mapOf(1 to toolId1.serialize())
        )

        val mappedDevice2 = Device(
            deviceId2.serialize(),
            678,
            "device2",
            "https://example.com/device2",
            "https://backup.example.com",
            mapOf(1 to toolId1.serialize(), 2 to toolId2.serialize())
        )

        // when
        val result = handleRequest(HttpMethod.Get, "/api/v1/device") {
            addBasicAuth(username, password)
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.OK)
        assertThat(result.response.content)
            .isNotNull()
            .isJson<Set<Device>>()
            .containsExactlyInAnyOrder(mappedDevice1, mappedDevice2)
    }

    @Test
    fun `given device exists when get device by id then returns mapped device`() = withConfiguredTestApp {
        // given
        val toolId = ToolIdFixture.arbitrary()

        val deviceId = DeviceIdFixture.arbitrary()
        val device = DeviceFixture.arbitrary(
            deviceId,
            345,
            "device",
            "https://example.com/device",
            "https://backup.example.com",
            "aabbccddee00",
            "secret00",
            mapOf(1 to toolId)
        )

        whenever(
            gettingDevice.getById(
                eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                any(),
                eq(deviceId)
            )
        ).thenReturn(device.right())

        val mappedDevice = Device(
            deviceId.serialize(),
            345,
            "device",
            "https://example.com/device",
            "https://backup.example.com",
            mapOf(1 to toolId.serialize())
        )

        // when
        val result = handleRequest(HttpMethod.Get, "/api/v1/device/${deviceId.serialize()}") {
            addBasicAuth(username, password)
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.OK)
        assertThat(result.response.content)
            .isNotNull()
            .isJson<Device>()
            .isEqualTo(mappedDevice)
    }

    @Test
    fun `given device not found when get device by id then returns mapped error`() = withConfiguredTestApp {
        // given
        val deviceId = DeviceIdFixture.arbitrary()
        val error = Error.DeviceNotFound("msg", deviceId)

        whenever(
            gettingDevice.getById(
                eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                any(),
                eq(deviceId)
            )
        ).thenReturn(error.left())

        // when
        val result = handleRequest(HttpMethod.Get, "/api/v1/device/${deviceId.serialize()}") {
            addBasicAuth(username, password)
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.NotFound)
        assertThat(result.response.content)
            .isError(
                "DeviceNotFound",
                "msg",
                mapOf("deviceId" to deviceId.serialize())
            )
    }
}