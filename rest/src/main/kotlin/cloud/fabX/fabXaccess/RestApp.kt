package cloud.fabX.fabXaccess

import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.rest.RestError
import cloud.fabX.fabXaccess.qualification.rest.QualificationController
import cloud.fabX.fabXaccess.user.rest.AuthenticationService
import cloud.fabX.fabXaccess.user.rest.UserController
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.auth.basic
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.serialization.json
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.serialization.json.Json

class RestApp(
    loggerFactory: LoggerFactory,
    private var publicPort: Int,
    private var authenticationService: AuthenticationService,
    private var qualificationController: QualificationController,
    private var userController: UserController
) {
    private val log: Logger = loggerFactory.invoke(this::class.java)

    val moduleConfiguration: Application.() -> Unit = {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = false
                isLenient = false
            })
        }

        install(StatusPages) {
            exception<kotlinx.serialization.SerializationException> { cause ->
                cause.printStackTrace()
                call.respond(HttpStatusCode.UnprocessableEntity, RestError(cause.localizedMessage))
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

        routing {
            authenticate("api-basic") {
                route("/api/v1") {
                    qualificationController.routes(this)
                    userController.routes(this)
                }
            }
        }
    }

    fun start() {
        log.debug("starting RestApp...")
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