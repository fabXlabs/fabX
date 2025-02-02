package cloud.fabX.fabXaccess.user.rest

import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.common.model.newCorrelationId
import cloud.fabX.fabXaccess.common.rest.handleError
import cloud.fabX.fabXaccess.common.rest.readBody
import cloud.fabX.fabXaccess.common.rest.respondWithErrorHandler
import cloud.fabX.fabXaccess.user.application.WebauthnIdentityService
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.util.toGMTDate
import io.ktor.util.date.toJvmDate
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import kotlinx.serialization.Serializable

class LoginController(
    private val webauthnIdentityService: WebauthnIdentityService,
    private val clock: Clock,
    private val jwtIssuer: String,
    private val jwtAudience: String,
    private val jwtHMAC256Secret: String,
    private val cookieDomain: String,
    private val cookiePath: String
) {

    val routes: Route.() -> Unit = {
        route("/login") {
            get("") {
                val setCookie: String? = call.queryParameters["cookie"]

                call.principal<UserPrincipal>()?.let { userPrincipal ->
                    if (userPrincipal.authenticationMethod == AuthenticationMethod.BASIC) {
                        val token = createJwt(userPrincipal.asMember().userId)

                        if (setCookie != null && setCookie == "true") {
                            call.response.cookies.append(
                                "FABX_AUTH",
                                token,
                                maxAge = 55.minutes.inWholeSeconds,
                                domain = cookieDomain,
                                path = cookiePath,
                                secure = true,
                                httpOnly = true
                            )
                            call.respond(HttpStatusCode.NoContent)
                        } else {
                            call.respond(TokenResponse(token))
                        }
                    } else {
                        call.respond(HttpStatusCode.Forbidden)
                    }
                }
                call.principal<ErrorPrincipal>()?.let { errorPrincipal ->
                    call.handleError(errorPrincipal.error)
                }
                call.handleError(Error.NotAuthenticated("Required authentication not found."))
            }
        }
    }

    private fun createJwt(userId: UserId): String =
        JWT.create()
            .withIssuer(jwtIssuer)
            .withAudience(jwtAudience)
            .withSubject(userId.serialize())
            .withExpiresAt(clock.now().plus(1.hours).toJavaInstant().toGMTDate().toJvmDate())
            .sign(Algorithm.HMAC256(jwtHMAC256Secret))

    val routesWebauthn: Route.() -> Unit = {
        route("/webauthn") {
            post("/login") {
                readBody<WebauthnLoginDetails>()
                    ?.let {
                        call.respondWithErrorHandler(
                            webauthnIdentityService.getLoginUserDetails(it.username)
                                .map { userLoginDetails ->
                                    WebauthnLoginResponse(
                                        userLoginDetails.userId.serialize(),
                                        userLoginDetails.challenge,
                                        userLoginDetails.credentialIds
                                    )
                                }
                        )
                    }
            }
            post("/response") {
                readBody<WebauthnResponseDetails>()
                    ?.let {
                        call.respondWithErrorHandler(
                            webauthnIdentityService.parseAndValidateAuthentication(
                                newCorrelationId(),
                                UserId.fromString(it.userId),
                                it.credentialId,
                                it.authenticatorData,
                                it.clientDataJSON,
                                it.signature
                            )
                                .map { userId ->
                                    val token = JWT.create()
                                        .withIssuer(jwtIssuer)
                                        .withAudience(jwtAudience)
                                        .withSubject(userId.serialize())
                                        .withExpiresAt(
                                            clock.now().plus(1.hours).toJavaInstant().toGMTDate().toJvmDate()
                                        )
                                        .sign(Algorithm.HMAC256(jwtHMAC256Secret))

                                    TokenResponse(token)
                                }
                        )
                    }
            }
        }
    }
}

@Serializable
data class TokenResponse(val token: String)