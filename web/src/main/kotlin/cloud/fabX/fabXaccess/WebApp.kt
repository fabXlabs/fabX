package cloud.fabX.fabXaccess

import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.rest.AppleAppSiteAssociation
import cloud.fabX.fabXaccess.common.rest.MetricsController
import cloud.fabX.fabXaccess.common.rest.MetricsPrincipal
import cloud.fabX.fabXaccess.common.rest.Slf4jLogger
import cloud.fabX.fabXaccess.common.rest.WebCredentials
import cloud.fabX.fabXaccess.common.rest.extractCause
import cloud.fabX.fabXaccess.device.rest.DeviceController
import cloud.fabX.fabXaccess.device.rest.DeviceFirmwareUpdateController
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
import io.ktor.server.application.call
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
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.forwardedheaders.XForwardedHeaders
import io.ktor.server.plugins.httpsredirect.HttpsRedirect
import io.ktor.server.plugins.origin
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.ApplicationRequest
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.micrometer.prometheus.PrometheusMeterRegistry
import java.time.Duration
import kotlinx.serialization.json.Json
import org.slf4j.MDC
import org.slf4j.event.Level

class WebApp(
    loggerFactory: LoggerFactory,
    private val publicPort: Int,
    private val jwtIssuer: String,
    private val jwtAudience: String,
    private val jwtHMAC256Secret: String,
    private val metricsPassword: String,
    private val httpsRedirect: Boolean,
    private val authenticationService: AuthenticationService,
    private val qualificationController: QualificationController,
    private val toolController: ToolController,
    private val deviceController: DeviceController,
    private val deviceWebsocketController: DeviceWebsocketController,
    private val deviceFirmwareUpdateController: DeviceFirmwareUpdateController,
    private val userController: UserController,
    private val loginController: LoginController,
    private val metricsController: MetricsController,
    private val appMicrometerRegistry: PrometheusMeterRegistry,
) {
    private val log: Logger = loggerFactory.invoke(this::class.java)

    private fun ApplicationRequest.toLogStringWithColors(): String = "${httpMethod.value} - ${path()}"

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

        if (httpsRedirect) {
            log.debug("https redirect enabled")
            install(HttpsRedirect) {
                exclude { it.request.origin.serverHost == "localhost" }
                exclude { it.request.origin.serverHost == "host.containers.internal" }
                exclude { it.request.path() == "/health" }
            }
        } else {
            log.debug("https redirect disabled")
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

        install(MicrometerMetrics) {
            registry = appMicrometerRegistry
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
            basic("metrics-basic") {
                realm = "fabX metrics"
                validate { credentials ->
                    if (credentials.name == "metrics"
                        && credentials.password == metricsPassword
                    ) {
                        MetricsPrincipal()
                    } else {
                        null
                    }
                }
            }
        }

        install(WebSockets) {
            pingPeriod = Duration.ofSeconds(15)
            timeout = Duration.ofSeconds(15)
            maxFrameSize = Long.MAX_VALUE
            masking = false
        }

        install(CallLogging) {
            logger = Slf4jLogger(log)
            level = Level.DEBUG
            disableDefaultColors()
            mdc("startTimestamp") { System.nanoTime().toString() }
            format { call ->
                val startTimestamp = MDC.get("startTimestamp").toLong()
                val endTimestamp = System.nanoTime()
                val delayMillis = (endTimestamp - startTimestamp) / 1_000_000.0

                when (val status = call.response.status() ?: "Unhandled") {
                    HttpStatusCode.Found -> "${status as HttpStatusCode} (${delayMillis}ms): " +
                            "${call.request.toLogStringWithColors()} -> ${call.response.headers[HttpHeaders.Location]}"

                    "Unhandled" -> "${status} (${delayMillis}ms): ${call.request.toLogStringWithColors()}"
                    else -> "${status as HttpStatusCode} (${delayMillis}ms): ${call.request.toLogStringWithColors()}"
                }
            }
            filter { call ->
                call.request.path().startsWith("/api")
            }
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
                    deviceFirmwareUpdateController.routes(this)
                    deviceWebsocketController.routes(this)
                    userController.routes(this)
                    loginController.routes(this)
                }
                loginController.routesWebauthn(this)
            }
            authenticate("metrics-basic") {
                metricsController.routes(this)
            }
            get("/health") {
                // TODO expand health checks, e.g. connection to database
                // TODO add tests
                call.respond(HttpStatusCode.OK)
            }
            get("/.well-known/apple-app-site-association") {
                // TODO make configurable
                call.respond(HttpStatusCode.OK, AppleAppSiteAssociation(WebCredentials(listOf("7V5294RF62.cloud.fabx.fabX"))))
            }
        }
    }

    fun start() {
        log.info("starting WebApp...")

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