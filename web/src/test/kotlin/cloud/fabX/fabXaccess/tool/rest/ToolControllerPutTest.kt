package cloud.fabX.fabXaccess.tool.rest

import arrow.core.None
import arrow.core.getOrElse
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.rest.ChangeableValue
import cloud.fabX.fabXaccess.common.rest.c
import cloud.fabX.fabXaccess.common.rest.isError
import cloud.fabX.fabXaccess.common.rest.withTestApp
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFixture
import cloud.fabX.fabXaccess.tool.application.ChangingTool
import cloud.fabX.fabXaccess.tool.model.ToolIdFixture
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
internal class ToolControllerPutTest {
    private lateinit var changingTool: ChangingTool
    private lateinit var authenticationService: AuthenticationService

    private val username = "some.one"
    private val password = "supersecret123"

    private val actingUser = UserFixture.arbitrary(isAdmin = true)

    @BeforeEach
    fun `configure WebModule`(
        @Mock changingTool: ChangingTool,
        @Mock authenticationService: AuthenticationService
    ) {
        this.changingTool = changingTool
        this.authenticationService = authenticationService
    }

    private fun withConfiguredTestApp(block: suspend ApplicationTestBuilder.() -> Unit) = withTestApp({
        bindInstance(overrides = true) { changingTool }
        bindInstance(overrides = true) { authenticationService }
    }, block)

    @Test
    fun `when changing tool then returns http no content`() = withConfiguredTestApp {
        // given
        val id = ToolIdFixture.arbitrary()

        val qualificationId1 = QualificationIdFixture.arbitrary()
        val qualificationId2 = QualificationIdFixture.arbitrary()

        val requestBody = ToolDetails(
            ChangeableValue("newName"),
            ChangeableValue(ToolType.UNLOCK),
            ChangeableValue(true),
            null,
            ChangeableValue(IdleState.IDLE_HIGH),
            ChangeableValue(false),
            null,
            null,
            ChangeableValue(setOf(qualificationId1.serialize(), qualificationId2.serialize()))
        )

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        whenever(
            changingTool.changeToolDetails(
                eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                any(),
                eq(id),
                eq(cloud.fabX.fabXaccess.common.model.ChangeableValue.ChangeToValueString("newName")),
                eq(cloud.fabX.fabXaccess.common.model.ChangeableValue.ChangeToValueToolType(cloud.fabX.fabXaccess.tool.model.ToolType.UNLOCK)),
                eq(cloud.fabX.fabXaccess.common.model.ChangeableValue.ChangeToValueBoolean(true)),
                eq(cloud.fabX.fabXaccess.common.model.ChangeableValue.LeaveAsIs),
                eq(cloud.fabX.fabXaccess.common.model.ChangeableValue.ChangeToValueIdleState(cloud.fabX.fabXaccess.tool.model.IdleState.IDLE_HIGH)),
                eq(cloud.fabX.fabXaccess.common.model.ChangeableValue.ChangeToValueBoolean(false)),
                eq(cloud.fabX.fabXaccess.common.model.ChangeableValue.LeaveAsIs),
                eq(cloud.fabX.fabXaccess.common.model.ChangeableValue.LeaveAsIs),
                eq(
                    cloud.fabX.fabXaccess.common.model.ChangeableValue.ChangeToValueQualificationSet(
                        setOf(qualificationId1, qualificationId2)
                    )
                )
            )
        ).thenReturn(None)

        // when
        val response = c().put("/api/v1/tool/${id.serialize()}") {
            basicAuth(username, password)
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NoContent)
        assertThat(response.bodyAsText()).isEmpty()
    }

    @Test
    fun `given no admin authentication when changing tool then returns http forbidden`() = withConfiguredTestApp {
        // given
        val requestBody = ToolDetails(
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        )

        val message = "abc1234"
        val error = Error.UserNotAdmin(message)

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(ErrorPrincipal(error))

        // when
        val response = c().put("/api/v1/tool/${ToolIdFixture.arbitrary().serialize()}") {
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
    fun `given no body when changing tool then returns http unprocessable entity`() = withConfiguredTestApp {
        // given
        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        // when
        val response = c().put("/api/v1/tool/${ToolIdFixture.arbitrary().serialize()}") {
            basicAuth(username, password)
            contentType(ContentType.Application.Json)
            // empty body
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.BadRequest)
    }
}