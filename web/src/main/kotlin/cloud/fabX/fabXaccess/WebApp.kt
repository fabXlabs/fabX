package cloud.fabX.fabXaccess

import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.rest.Error
import cloud.fabX.fabXaccess.device.rest.DeviceController
import cloud.fabX.fabXaccess.device.ws.DeviceWebsocketController
import cloud.fabX.fabXaccess.qualification.rest.QualificationController
import cloud.fabX.fabXaccess.tool.rest.ToolController
import cloud.fabX.fabXaccess.user.rest.AuthenticationService
import cloud.fabX.fabXaccess.user.rest.UserController
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.plugins.spa.SinglePageApplication
import io.ktor.plugins.spa.angular
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.basic
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.CORS
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
    private val authenticationService: AuthenticationService,
    private val qualificationController: QualificationController,
    private val toolController: ToolController,
    private val deviceController: DeviceController,
    private val deviceWebsocketController: DeviceWebsocketController,
    private val userController: UserController
) {
    private val log: Logger = loggerFactory.invoke(this::class.java)

    val moduleConfiguration: Application.() -> Unit = {
        install(CORS) {
            method(HttpMethod.Options)
            method(HttpMethod.Get)
            method(HttpMethod.Post)
            method(HttpMethod.Put)
            method(HttpMethod.Delete)
            method(HttpMethod.Patch)

            header(HttpHeaders.Accept)
            header(HttpHeaders.AccessControlRequestHeaders)
            header(HttpHeaders.AccessControlRequestMethod)
            header(HttpHeaders.ContentType)
            header(HttpHeaders.XForwardedProto)
            header(HttpHeaders.Origin)
            header(HttpHeaders.Referrer)
            header(HttpHeaders.UserAgent)
            header(HttpHeaders.Authorization)

            anyHost()

            allowNonSimpleContentTypes = true
        }

        install(ContentNegotiation) {
            json(Json {
                prettyPrint = false
                isLenient = false
            })
        }

        install(StatusPages) {
            exception<kotlinx.serialization.SerializationException> { call, cause ->
                cause.printStackTrace()
                call.respond(
                    HttpStatusCode.UnprocessableEntity,
                    Error(cause::class.qualifiedName ?: "unknown", cause.localizedMessage, mapOf(), null)
                )
            }
        }

        install(Authentication) {
            basic("api-basic") {
                realm = "fabX"
                validate { credentials ->
                    authenticationService.basic(credentials)
                }
            }
        }

        install(WebSockets) {
            pingPeriod = Duration.ofSeconds(15)
            timeout = Duration.ofSeconds(15)
            maxFrameSize = Long.MAX_VALUE
            masking = false
        }

        // TODO wait for fix: https://youtrack.jetbrains.com/issue/KTOR-515
        install(SinglePageApplication) {
            angular("fabx-dashboard")
            useResources = true
        }

        routing {
            authenticate("api-basic") {
                route("/api/v1") {
                    qualificationController.routes(this)
                    toolController.routes(this)
                    deviceController.routes(this)
                    deviceWebsocketController.routes(this)
                    userController.routes(this)
                }
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