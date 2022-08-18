package cloud.fabX.fabXaccess.user.rest

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.util.toGMTDate
import io.ktor.util.date.toJvmDate
import kotlin.time.Duration.Companion.hours
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import kotlinx.serialization.Serializable

class LoginController(
    private val clock: Clock,
    private val jwtIssuer: String,
    private val jwtAudience: String,
    private val jwtHMAC256Secret: String,
) {

    val routes: Route.() -> Unit = {
        route("/login") {
            get("") {
                call.principal<UserPrincipal>()?.let { userPrincipal ->
                    if (userPrincipal.authenticationMethod == AuthenticationMethod.BASIC) {
                        val userId = userPrincipal.asMember().userId

                        val token = JWT.create()
                            .withIssuer(jwtIssuer)
                            .withAudience(jwtAudience)
                            .withSubject(userId.serialize())
                            .withExpiresAt(clock.now().plus(1.hours).toJavaInstant().toGMTDate().toJvmDate())
                            .sign(Algorithm.HMAC256(jwtHMAC256Secret))

                        call.respond(TokenResponse(token))
                    } else {
                        call.respond(HttpStatusCode.Forbidden)
                    }
                }
            }
        }
    }
}

@Serializable
data class TokenResponse(val token: String)