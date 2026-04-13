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
import cloud.fabX.fabXaccess.device.application.GettingDevicePinStatus
import cloud.fabX.fabXaccess.device.model.DeviceIdFixture
import cloud.fabX.fabXaccess.device.model.DevicePinStatus
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
internal class DeviceControllerPinStatusTest {
    private lateinit var gettingDevicePinStatus: GettingDevicePinStatus
    private lateinit var authenticationService: AuthenticationService

    private val username = "some.one"
    private val password = "supersecret123"

    private val actingUser = UserFixture.arbitrary(isAdmin = true)

    @BeforeEach
    fun `configure WebModule`(
        @Mock gettingDevicePinStatus: GettingDevicePinStatus,
        @Mock authenticationService: AuthenticationService
    ) {
        this.gettingDevicePinStatus = gettingDevicePinStatus
        this.authenticationService = authenticationService

        runTest {
            whenever(authenticationService.basic(UserPasswordCredential(username, password)))
                .thenReturn(UserPrincipal(actingUser))
        }
    }

    private fun withConfiguredTestApp(block: suspend ApplicationTestBuilder.() -> Unit) = withTestApp({
        bindInstance(overrides = true) { gettingDevicePinStatus }
        bindInstance(overrides = true) { authenticationService }
    }, block)

    @Test
    fun `given no device status when get device pin statuses then returns empty map`() = withConfiguredTestApp {
        // given
        whenever(
            gettingDevicePinStatus.getAll(
                eq(actingUser.asMember()),
                any()
            )
        )
            .thenReturn(emptySet())

        // when
        val response = c().get("/api/v1/device/pin-status") {
            basicAuth(username, password)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.body<Map<String, Map<Int, Boolean>>>())
            .isEmpty()
    }

    @Test
    fun `when get device pin statuses then returns status map`() = withConfiguredTestApp {
        // given
        val deviceId1 = DeviceIdFixture.arbitrary()
        val deviceId2 = DeviceIdFixture.arbitrary()
        val deviceId3 = DeviceIdFixture.arbitrary()

        whenever(
            gettingDevicePinStatus.getAll(
                eq(actingUser.asMember()),
                any()
            )
        )
            .thenReturn(
                setOf(
                    DevicePinStatus(
                        deviceId1,
                        mapOf(1 to true, 2 to false)
                    ),
                    DevicePinStatus(
                        deviceId2,
                        mapOf(1 to true)
                    ),
                    DevicePinStatus(
                        deviceId3,
                        mapOf(4 to true)
                    )
                )
            )

        // when
        val response = c().get("/api/v1/device/pin-status") {
            basicAuth(username, password)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.body<Map<String, Map<Int, Boolean>>>())
            .containsOnly(
                deviceId1.serialize() to mapOf(1 to true, 2 to false),
                deviceId2.serialize() to mapOf(1 to true),
                deviceId3.serialize() to mapOf(4 to true)
            )
    }

    @Test
    fun `when get device pin status by id then returns status map`() = withConfiguredTestApp {
        // given
        val deviceId = DeviceIdFixture.arbitrary()

        whenever(
            gettingDevicePinStatus.getById(
                eq(actingUser.asMember()),
                any(),
                eq(deviceId)
            )
        )
            .thenReturn(
                    DevicePinStatus(
                        deviceId,
                        mapOf(1 to true, 2 to false)
                    ).right()
            )

        // when
        val response = c().get("/api/v1/device/${deviceId.serialize()}/pin-status") {
            basicAuth(username, password)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.body<Map<Int, Boolean>>())
            .isEqualTo(mapOf(1 to true, 2 to false))
    }

    @Test
    fun `given device does not exist when get device pin status by id then returns mapped error`() = withConfiguredTestApp {
        // given
        val deviceId = DeviceIdFixture.arbitrary()
        val error = Error.DeviceNotFound("msg", deviceId)

        whenever(
            gettingDevicePinStatus.getById(
                eq(actingUser.asMember()),
                any(),
                eq(deviceId)
            )
        ).thenReturn(error.left())

        // when
        val response = c().get("/api/v1/device/${deviceId.serialize()}/pin-status") {
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
