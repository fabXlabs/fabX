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
import cloud.fabX.fabXaccess.device.application.AddingCardIdentityAtDevice
import cloud.fabX.fabXaccess.device.model.DeviceIdFixture
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.model.UserIdFixture
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
class DeviceControllerAddCardIdentityAtDeviceTest {
    private lateinit var addingCardIdentityAtDevice: AddingCardIdentityAtDevice
    private lateinit var authenticationService: AuthenticationService

    private val username = "some.one"
    private val password = "supersecret123"

    private val actingUser = UserFixture.arbitrary(isAdmin = true)

    @BeforeEach
    fun `configure WebModule`(
        @Mock addingCardIdentityAtDevice: AddingCardIdentityAtDevice,
        @Mock authenticationService: AuthenticationService
    ) {
        this.addingCardIdentityAtDevice = addingCardIdentityAtDevice
        this.authenticationService = authenticationService
    }

    private fun withConfiguredTestApp(block: suspend ApplicationTestBuilder.() -> Unit) = withTestApp({
        bindInstance(overrides = true) { addingCardIdentityAtDevice }
        bindInstance(overrides = true) { authenticationService }
    }, block)

    @Test
    fun `when adding card identity at device then returns http no content`() = withConfiguredTestApp {
        // given
        val deviceId = DeviceIdFixture.arbitrary()

        val userId = UserIdFixture.arbitrary()
        val requestBody = AtDeviceCardCreationDetails(userId.serialize())

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        whenever(
            addingCardIdentityAtDevice.addCardIdentityAtDevice(
                eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                any(),
                eq(deviceId),
                eq(userId)
            )
        )
            .thenReturn(Unit.right())

        // when
        val response = c().post("/api/v1/device/${deviceId.serialize()}/add-user-card-identity") {
            basicAuth(username, password)
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NoContent)
        assertThat(response.bodyAsText()).isEmpty()
    }

    @Test
    fun `given no admin authentication when adding card identity at device then returns http forbidden`() =
        withConfiguredTestApp {
            // given
            val requestBody = AtDeviceCardCreationDetails(UserIdFixture.arbitrary().serialize())

            val message = "abc1234"
            val error = Error.UserNotAdmin(message)

            whenever(authenticationService.basic(UserPasswordCredential(username, password)))
                .thenReturn(ErrorPrincipal(error))

            // when
            val response =
                c().post("/api/v1/device/${DeviceIdFixture.arbitrary().serialize()}/add-user-card-identity") {
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
    fun `given invalid device id when adding card identity at device then returns http bad request`() =
        withConfiguredTestApp {
            // given
            val invalidDeviceId = "invalidDeviceId"

            val requestBody = AtDeviceCardCreationDetails(UserIdFixture.arbitrary().serialize())

            whenever(authenticationService.basic(UserPasswordCredential(username, password)))
                .thenReturn(UserPrincipal(actingUser))

            // when
            val response =
                c().post("/api/v1/device/$invalidDeviceId/add-user-card-identity") {
                    basicAuth(username, password)
                    contentType(ContentType.Application.Json)
                    setBody(requestBody)
                }

            // then
            assertThat(response.status).isEqualTo(HttpStatusCode.BadRequest)
            assertThat(response.body<String>()).isEqualTo("Required UUID parameter \"id\" not given or invalid.")
        }

    @Test
    fun `given domain error when adding card identity at device then returns http no content`() =
        withConfiguredTestApp {
            // given
            val deviceId = DeviceIdFixture.arbitrary()

            val userId = UserIdFixture.arbitrary()
            val requestBody = AtDeviceCardCreationDetails(userId.serialize())

            val correlationId = CorrelationIdFixture.arbitrary()
            val error = Error.DeviceNotConnected("msg", deviceId, correlationId)

            whenever(authenticationService.basic(UserPasswordCredential(username, password)))
                .thenReturn(UserPrincipal(actingUser))

            whenever(
                addingCardIdentityAtDevice.addCardIdentityAtDevice(
                    eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                    any(),
                    eq(deviceId),
                    eq(userId)
                )
            )
                .thenReturn(error.left())

            // when
            val response = c().post("/api/v1/device/${deviceId.serialize()}/add-user-card-identity") {
                basicAuth(username, password)
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            // then
            assertThat(response.status).isEqualTo(HttpStatusCode.ServiceUnavailable)
            assertThat(response.body<cloud.fabX.fabXaccess.common.rest.Error>())
                .isError(
                    "DeviceNotConnected",
                    "msg",
                    mapOf("deviceId" to deviceId.serialize()),
                    correlationId.serialize()
                )
        }
}
