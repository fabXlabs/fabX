package cloud.fabX.fabXaccess.device.rest

import arrow.core.None
import arrow.core.getOrElse
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.rest.c
import cloud.fabX.fabXaccess.common.rest.isError
import cloud.fabX.fabXaccess.common.rest.withTestApp
import cloud.fabX.fabXaccess.device.application.ChangingThumbnail
import cloud.fabX.fabXaccess.device.application.GettingDevice
import cloud.fabX.fabXaccess.device.model.DeviceIdFixture
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.rest.AuthenticationService
import cloud.fabX.fabXaccess.user.rest.ErrorPrincipal
import cloud.fabX.fabXaccess.user.rest.UserPrincipal
import io.ktor.client.call.body
import io.ktor.client.request.basicAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HeaderValue
import io.ktor.http.HttpStatusCode
import io.ktor.http.cacheControl
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
internal class DeviceControllerThumbnailTest {
    private lateinit var gettingDevice: GettingDevice
    private lateinit var changingThumbnail: ChangingThumbnail
    private lateinit var authenticationService: AuthenticationService

    private val username = "some.one"
    private val password = "supersecret123"

    private val actingUser = UserFixture.arbitrary(isAdmin = true)

    @BeforeEach
    fun `configure WebModule`(
        @Mock gettingDevice: GettingDevice,
        @Mock changingThumbnail: ChangingThumbnail,
        @Mock authenticationService: AuthenticationService
    ) {
        this.gettingDevice = gettingDevice
        this.changingThumbnail = changingThumbnail
        this.authenticationService = authenticationService
    }

    private fun withConfiguredTestApp(block: suspend ApplicationTestBuilder.() -> Unit) = withTestApp({
        bindInstance(overrides = true) { gettingDevice }
        bindInstance(overrides = true) { changingThumbnail }
        bindInstance(overrides = true) { authenticationService }
    }, block)

    @Test
    fun `when getting thumbnail then returns thumbnail`() = withConfiguredTestApp {
        // given
        val deviceId = DeviceIdFixture.arbitrary()

        val thumbnailData = ByteArray(5) { it.toByte() }

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        whenever(
            gettingDevice.getThumbnail(
                eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                any(),
                eq(deviceId)
            )
        ).thenReturn(thumbnailData.right())

        // when
        val response = c().get("/api/v1/device/${deviceId.serialize()}/thumbnail") {
            basicAuth(username, password)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.body<ByteArray>()).isEqualTo(thumbnailData)
        assertThat(response.cacheControl()).containsExactlyInAnyOrder(HeaderValue("max-age=60"), HeaderValue("must-revalidate"), HeaderValue("private"))
    }

    @Test
    fun `given no admin authentication when getting thumbnail then returns http forbidden`() = withConfiguredTestApp {
        // given
        val message = "abc1234"
        val error = Error.UserNotAdmin(message)

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(ErrorPrincipal(error))

        // when
        val response = c().get("/api/v1/device/${DeviceIdFixture.arbitrary().serialize()}/thumbnail") {
            basicAuth(username, password)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.Forbidden)
        assertThat(response.body<cloud.fabX.fabXaccess.common.rest.Error>())
            .isError(
                type = "UserNotAdmin",
                message = message
            )
    }

    @Test
    fun `when changing thumbnail then returns ok`() = withConfiguredTestApp {
        // given
        val deviceId = DeviceIdFixture.arbitrary()

        val requestBody = ByteArray(5) { it.toByte() }

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        whenever(
            changingThumbnail.changeDeviceThumbnail(
                eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                any(),
                eq(deviceId),
                eq(requestBody)
            )
        ).thenReturn(None)

        // when
        val response = c().post("/api/v1/device/${deviceId.serialize()}/thumbnail") {
            basicAuth(username, password)
            contentType(ContentType.Image.JPEG)
            setBody(requestBody)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NoContent)
        assertThat(response.bodyAsText()).isEmpty()
    }

    @Test
    fun `given no admin authentication when changing thumbnail then returns http forbidden`() = withConfiguredTestApp {
        // given
        val requestBody = ByteArray(5) { it.toByte() }

        val message = "abc1234"
        val error = Error.UserNotAdmin(message)

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(ErrorPrincipal(error))

        // when
        val response = c().post("/api/v1/device/${DeviceIdFixture.arbitrary().serialize()}/thumbnail") {
            basicAuth(username, password)
            contentType(ContentType.Image.JPEG)
            setBody(requestBody)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.Forbidden)
        assertThat(response.body<cloud.fabX.fabXaccess.common.rest.Error>())
            .isError(
                type = "UserNotAdmin",
                message = message
            )
    }
}