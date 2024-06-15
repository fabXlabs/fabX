package cloud.fabX.fabXaccess.tool.rest

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
import cloud.fabX.fabXaccess.tool.application.ChangingThumbnail
import cloud.fabX.fabXaccess.tool.application.GettingTool
import cloud.fabX.fabXaccess.tool.model.ToolIdFixture
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
internal class ToolControllerThumbnailTest {
    private lateinit var gettingTool: GettingTool
    private lateinit var changingThumbnail: ChangingThumbnail
    private lateinit var authenticationService: AuthenticationService

    private val username = "some.one"
    private val password = "supersecret123"

    private val actingUser = UserFixture.arbitrary(isAdmin = true)

    @BeforeEach
    fun `configure WebModule`(
        @Mock gettingTool: GettingTool,
        @Mock changingThumbnail: ChangingThumbnail,
        @Mock authenticationService: AuthenticationService
    ) {
        this.gettingTool = gettingTool
        this.changingThumbnail = changingThumbnail
        this.authenticationService = authenticationService
    }

    private fun withConfiguredTestApp(block: suspend ApplicationTestBuilder.() -> Unit) = withTestApp({
        bindInstance(overrides = true) { gettingTool }
        bindInstance(overrides = true) { changingThumbnail }
        bindInstance(overrides = true) { authenticationService }
    }, block)

    @Test
    fun `when getting thumbnail then returns thumbnail`() = withConfiguredTestApp {
        // given
        val toolId = ToolIdFixture.arbitrary()

        val thumbnailData = ByteArray(5) { it.toByte() }

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        whenever(
            gettingTool.getThumbnail(
                eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                any(),
                eq(toolId)
            )
        ).thenReturn(thumbnailData.right())

        // when
        val response = c().get("/api/v1/tool/${toolId.serialize()}/thumbnail") {
            basicAuth(username, password)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.body<ByteArray>()).isEqualTo(thumbnailData)
        assertThat(response.cacheControl()).containsExactlyInAnyOrder(
            HeaderValue("max-age=60"),
            HeaderValue("must-revalidate"),
            HeaderValue("private")
        )
    }

    @Test
    fun `given no admin authentication when getting thumbnail then returns http forbidden`() = withConfiguredTestApp {
        // given
        val message = "abc1234"
        val error = Error.UserNotAdmin(message)

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(ErrorPrincipal(error))

        // when
        val response = c().get("/api/v1/tool/${ToolIdFixture.arbitrary().serialize()}/thumbnail") {
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
        val toolId = ToolIdFixture.arbitrary()

        val requestBody = ByteArray(5) { it.toByte() }

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        whenever(
            changingThumbnail.changeToolThumbnail(
                eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                any(),
                eq(toolId),
                eq(requestBody)
            )
        ).thenReturn(Unit.right())

        // when
        val response = c().post("/api/v1/tool/${toolId.serialize()}/thumbnail") {
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
        val response = c().post("/api/v1/tool/${ToolIdFixture.arbitrary().serialize()}/thumbnail") {
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