package cloud.fabX.fabXaccess.user.rest

import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.newCorrelationId
import cloud.fabX.fabXaccess.common.model.newUserId
import cloud.fabX.fabXaccess.common.rest.c
import cloud.fabX.fabXaccess.common.rest.isError
import cloud.fabX.fabXaccess.common.rest.withTestApp
import cloud.fabX.fabXaccess.user.application.GettingUserSourcingEvents
import cloud.fabX.fabXaccess.user.model.UserCreated
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.model.UserPersonalInformationChanged
import cloud.fabX.fabXaccess.user.model.UserSourcingEvent
import io.ktor.client.call.body
import io.ktor.client.request.basicAuth
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.UserPasswordCredential
import io.ktor.server.testing.ApplicationTestBuilder
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.kodein.di.bindInstance
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever

@MockitoSettings
class UserControllerGetSourcingEventsTest {
    private lateinit var gettingUserSourcingEvents: GettingUserSourcingEvents
    private lateinit var authenticationService: AuthenticationService

    private val username = "some.one"
    private val password = "some.password"

    private val actingUser = UserFixture.arbitrary(isAdmin = true)

    @BeforeEach
    fun `configure WebModule`(
        @Mock gettingUserSourcingEvents: GettingUserSourcingEvents,
        @Mock authenticationService: AuthenticationService
    ) {
        this.gettingUserSourcingEvents = gettingUserSourcingEvents
        this.authenticationService = authenticationService

        runTest {
            whenever(authenticationService.basic(UserPasswordCredential(username, password)))
                .thenReturn(UserPrincipal(actingUser))
        }
    }

    private fun withConfiguredTestApp(block: suspend ApplicationTestBuilder.() -> Unit) = withTestApp({
        bindInstance(overrides = true) { gettingUserSourcingEvents }
        bindInstance(overrides = true) { authenticationService }
    }, block)

    @Test
    fun `given no sourcing events when get sourcing events then returns empty list`() = withConfiguredTestApp {
        // given
        whenever(
            gettingUserSourcingEvents.getAll(
                eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                any()
            )
        ).thenReturn(emptyList())

        // when
        val response = c().get("/api/v1/user/sourcing-event") {
            basicAuth(username, password)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.body<List<UserSourcingEvent>>()).isEmpty()
    }

    @Test
    fun `when get sourcing events then returns sourcing events`() = withConfiguredTestApp {
        // given
        val sourcingEvents = listOf(
            UserCreated(
                aggregateRootId = newUserId(),
                actorId = newUserId(),
                timestamp = Instant.fromEpochSeconds(1720088380, 0),
                newCorrelationId(),
                "hello",
                "world",
                "hello.world"
            ),
            UserCreated(
                aggregateRootId = newUserId(),
                actorId = newUserId(),
                timestamp = Instant.fromEpochSeconds(1720088381, 0),
                newCorrelationId(),
                "some",
                "body",
                "some.body"
            ),
        )

        whenever(
            gettingUserSourcingEvents.getAll(
                eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                any()
            )
        ).thenReturn(sourcingEvents)

        // when
        val response = c().get("/api/v1/user/sourcing-event") {
            basicAuth(username, password)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.body<List<UserSourcingEvent>>()).isEqualTo(sourcingEvents)
    }

    @Test
    fun `when get sourcing events by id then returns sourcing events`() = withConfiguredTestApp {
        // given
        val userId = newUserId()

        val sourcingEvents = listOf(
            UserCreated(
                aggregateRootId = userId,
                actorId = newUserId(),
                timestamp = Instant.fromEpochSeconds(1720088380, 0),
                newCorrelationId(),
                "hello",
                "world",
                "hello.world"
            ),
            UserPersonalInformationChanged(
                aggregateRootId = userId,
                aggregateVersion = 2,
                actorId = newUserId(),
                timestamp = Instant.fromEpochSeconds(1720088381, 0),
                correlationId = newCorrelationId(),
                firstName = ChangeableValue.ChangeToValueString("hi"),
                lastName = ChangeableValue.LeaveAsIs,
                wikiName = ChangeableValue.LeaveAsIs
            )
        )

        whenever(
            gettingUserSourcingEvents.getById(
                eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                any(),
                eq(userId)
            )
        ).thenReturn(sourcingEvents.right())

        // when
        val response = c().get("/api/v1/user/${userId.serialize()}/sourcing-event") {
            basicAuth(username, password)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.body<List<UserSourcingEvent>>()).isEqualTo(sourcingEvents)
    }

    @Test
    fun `given domain error when get sourcing events by id then returns mapped error`() = withConfiguredTestApp {
        // given
        val userId = newUserId()
        val error = Error.UserNotFound("msg", userId)

        whenever(
            gettingUserSourcingEvents.getById(
                eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                any(),
                eq(userId)
            )
        ).thenReturn(error.left())

        // when
        val response = c().get("/api/v1/user/${userId.serialize()}/sourcing-event") {
            basicAuth(username, password)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NotFound)
        assertThat(response.body<cloud.fabX.fabXaccess.common.rest.Error>())
            .isError(
                "UserNotFound",
                "msg",
                mapOf("userId" to userId.serialize())
            )
    }
}