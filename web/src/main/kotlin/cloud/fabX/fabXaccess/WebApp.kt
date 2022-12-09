package cloud.fabX.fabXaccess

import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.rest.extractCause
import cloud.fabX.fabXaccess.device.rest.DeviceController
import cloud.fabX.fabXaccess.device.ws.DeviceWebsocketController
import cloud.fabX.fabXaccess.qualification.rest.QualificationController
import cloud.fabX.fabXaccess.tool.rest.ToolController
import cloud.fabX.fabXaccess.user.rest.AuthenticationService
import cloud.fabX.fabXaccess.user.rest.LoginController
import cloud.fabX.fabXaccess.user.rest.UserController
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.basic
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.angular
import io.ktor.server.http.content.singlePageApplication
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.forwardedheaders.XForwardedHeaders
import io.ktor.server.plugins.httpsredirect.HttpsRedirect
import io.ktor.server.plugins.origin
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import java.time.Duration
import kotlinx.serialization.json.Json

class WebApp(
    loggerFactory: LoggerFactory,
    private val publicPort: Int,
    private val jwtIssuer: String,
    private val jwtAudience: String,
    private val jwtHMAC256Secret: String,
    private val authenticationService: AuthenticationService,
    private val qualificationController: QualificationController,
    private val toolController: ToolController,
    private val deviceController: DeviceController,
    private val deviceWebsocketController: DeviceWebsocketController,
    private val userController: UserController,
    private val loginController: LoginController
) {
    private val log: Logger = loggerFactory.invoke(this::class.java)

    val moduleConfiguration: Application.() -> Unit = {
        install(io.ktor.server.plugins.cors.routing.CORS) {
            allowMethod(HttpMethod.Options)
            allowMethod(HttpMethod.Get)
            allowMethod(HttpMethod.Post)
            allowMethod(HttpMethod.Put)
            allowMethod(HttpMethod.Delete)
            allowMethod(HttpMethod.Patch)

            allowHeader(HttpHeaders.Accept)
            allowHeader(HttpHeaders.AccessControlRequestHeaders)
            allowHeader(HttpHeaders.AccessControlRequestMethod)
            allowHeader(HttpHeaders.ContentType)
            allowHeader(HttpHeaders.XForwardedProto)
            allowHeader(HttpHeaders.Origin)
            allowHeader(HttpHeaders.Referrer)
            allowHeader(HttpHeaders.UserAgent)
            allowHeader(HttpHeaders.Authorization)

            anyHost()

            allowNonSimpleContentTypes = true
        }

        install(HttpsRedirect) {
            exclude { it.request.origin.serverHost == "localhost" }
        }
        install(XForwardedHeaders)

        install(ContentNegotiation) {
            json(Json {
                prettyPrint = false
                isLenient = false
            })
        }

        install(StatusPages) {
            exception<kotlinx.serialization.SerializationException> { call, cause ->
                call.respond(HttpStatusCode.BadRequest, cause.toString())
            }
            exception<BadRequestException> { call, cause ->
                call.respond(HttpStatusCode.BadRequest, extractCause(cause))
            }
        }

        install(Authentication) {
            basic("api-basic") {
                realm = "fabX"
                validate { credentials ->
                    authenticationService.basic(credentials)
                }
            }
            jwt("api-jwt") {
                realm = "fabX"

                verifier(
                    JWT.require(Algorithm.HMAC256(jwtHMAC256Secret))
                        .withIssuer(jwtIssuer)
                        .withAudience(jwtAudience)
                        .build()
                )

                validate { jwtCredential ->
                    jwtCredential.subject?.let { authenticationService.jwt(it) }
                }
            }
        }

        install(WebSockets) {
            pingPeriod = Duration.ofSeconds(15)
            timeout = Duration.ofSeconds(15)
            maxFrameSize = Long.MAX_VALUE
            masking = false
        }

        routing {
            singlePageApplication {
                angular("fabx-dashboard")
                useResources = true
            }
            route("/api/v1") {
                authenticate("api-basic", "api-jwt") {
                    qualificationController.routes(this)
                    toolController.routes(this)
                    deviceController.routes(this)
                    deviceWebsocketController.routes(this)
                    userController.routes(this)
                    loginController.routes(this)
                }
                loginController.routesWebauthn(this)
            }
        }
    }

    fun start() {
        log.debug("starting WebApp...")
        embeddedServer(Netty, environment = applicationEngineEnvironment {

            module {
                moduleConfiguration()
            }

            connector {
                port = publicPort
            }

        }).start(wait = true)
    }
}