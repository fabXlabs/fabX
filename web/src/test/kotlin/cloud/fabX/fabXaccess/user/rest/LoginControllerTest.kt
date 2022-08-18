package cloud.fabX.fabXaccess.user.rest

import FixedClock
import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.rest.c
import cloud.fabX.fabXaccess.common.rest.withTestApp
import cloud.fabX.fabXaccess.user.model.UserFixture
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.call.body
import io.ktor.client.request.basicAuth
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.UserPasswordCredential
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.util.toGMTDate
import io.ktor.util.date.toJvmDate
import kotlin.time.Duration.Companion.hours
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.kodein.di.bindConstant
import org.kodein.di.bindInstance
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.whenever

@MockitoSettings
internal class LoginControllerTest {
    private lateinit var authenticationService: AuthenticationService

    private val username = "some.one"
    private val password = "supersecret123"

    private val actingUser = UserFixture.arbitrary()

    private val fixedInstant = Clock.System.now()
    private val fixedClock = FixedClock(fixedInstant)

    private val jwtIssuer = "https://example.com/"
    private val jwtAudience = "https://example.com"
    private val jwtHMAC256Secret = "verysecret"

    @BeforeEach
    fun `configure WebModule`(
        @Mock authenticationService: AuthenticationService
    ) {
        this.authenticationService = authenticationService
    }

    private fun withConfiguredTestApp(block: suspend ApplicationTestBuilder.() -> Unit) = withTestApp({
        bindInstance(overrides = true) { authenticationService }
        bindInstance<Clock>(overrides = true) { fixedClock }
        bindConstant(tag = "jwtIssuer", overrides = true) { jwtIssuer }
        bindConstant(tag = "jwtAudience", overrides = true) { jwtAudience }
        bindConstant(tag = "jwtHMAC256Secret", overrides = true) { jwtHMAC256Secret }
    }, block)

    @Test
    fun `given basic authentication when login then returns token`() = withConfiguredTestApp {
        // given
        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser, AuthenticationMethod.BASIC))

        // when
        val response = c().get("/api/v1/login") {
            basicAuth(username, password)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.body<TokenResponse>())
            .transform { it.token }
            .transform { JWT.require(Algorithm.HMAC256(jwtHMAC256Secret)).build().verify(it) }
            .all {
                transform { it.subject }.isEqualTo(actingUser.id.serialize())
                transform { it.audience }.containsExactly(jwtAudience)
                transform { it.issuer }.isEqualTo(jwtIssuer)
                transform { it.expiresAt }.isEqualTo(fixedInstant.plus(1.hours).toJavaInstant().toGMTDate().toJvmDate())
            }
    }

    @Test
    fun `given jwt authentication when login then returns http forbidden`() = withConfiguredTestApp {
        // given
        whenever(authenticationService.jwt(actingUser.id.serialize()))
            .thenReturn(UserPrincipal(actingUser, AuthenticationMethod.JWT))

        val token = JWT.create()
            .withAudience(jwtAudience)
            .withIssuer(jwtIssuer)
            .withSubject(actingUser.id.serialize())
            .withExpiresAt(fixedInstant.plus(1.hours).toJavaInstant().toGMTDate().toJvmDate())
            .sign(Algorithm.HMAC256(jwtHMAC256Secret))

        // when
        val response = c().get("/api/v1/login") {
            bearerAuth(token)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.Forbidden)
        assertThat(response.bodyAsText()).isEmpty()
    }
}