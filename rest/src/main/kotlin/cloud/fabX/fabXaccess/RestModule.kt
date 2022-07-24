package cloud.fabX.fabXaccess

import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.rest.RestError
import cloud.fabX.fabXaccess.qualification.application.AddingQualification
import cloud.fabX.fabXaccess.qualification.application.DeletingQualification
import cloud.fabX.fabXaccess.qualification.application.GettingQualification
import cloud.fabX.fabXaccess.qualification.rest.QualificationController
import cloud.fabX.fabXaccess.user.application.GettingUser
import cloud.fabX.fabXaccess.user.application.GettingUserByIdentity
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

object RestModule {
    // external configuration
    private var publicPort: Int? = null
    private var loggerFactory: LoggerFactory? = null
    private var gettingUser: GettingUser? = null
    private var gettingUserByIdentity: GettingUserByIdentity? = null
    private var gettingQualification: GettingQualification? = null
    private var addingQualification: AddingQualification? = null
    private var deletingQualification: DeletingQualification? = null

    // internal
    private var authenticationService: AuthenticationService? = null

    // controller
    private var qualificationController: QualificationController? = null
    private var userController: UserController? = null

    fun isFullyConfigured(): Boolean {
        return try {
            require(publicPort)
            require(loggerFactory)
            require(gettingUser)
            require(gettingUserByIdentity)
            require(gettingQualification)
            require(addingQualification)
            require(deletingQualification)

            true
        } catch (e: IllegalArgumentException) {
            System.err.println(e.message)
            false
        }
    }

    fun configurePort(port: Int) {
        this.publicPort = port
    }

    fun configureLoggerFactory(loggerFactory: LoggerFactory) {
        this.loggerFactory = loggerFactory
    }

    fun configureGettingUser(gettingUser: GettingUser) {
        this.gettingUser = gettingUser
    }

    fun configureGettingUserByIdentity(gettingUserByIdentity: GettingUserByIdentity) {
        this.gettingUserByIdentity = gettingUserByIdentity
    }

    fun configureGettingQualification(gettingQualification: GettingQualification) {
        this.gettingQualification = gettingQualification
    }

    fun configureAddingQualification(addingQualification: AddingQualification) {
        this.addingQualification = addingQualification
    }

    fun configureDeletingQualification(deletingQualification: DeletingQualification) {
        this.deletingQualification = deletingQualification
    }

    internal fun loggerFactory(): LoggerFactory {
        return require(loggerFactory)
    }

    /**
     * Only for testing!
     */
    internal fun overrideAuthenticationService(authenticationService: AuthenticationService) {
        this.authenticationService = authenticationService
    }

    private fun authenticationService(): AuthenticationService {
        val instance = authenticationService

        return if (instance != null) {
            instance
        } else {
            val newInstance = AuthenticationService(
                require(gettingUserByIdentity)
            )
            authenticationService = newInstance
            newInstance
        }
    }

    private fun qualificationController(): QualificationController {
        val instance = qualificationController

        return if (instance != null) {
            instance
        } else {
            val newInstance = QualificationController(
                require(gettingQualification),
                require(addingQualification),
                require(deletingQualification)
            )
            qualificationController = newInstance
            newInstance
        }
    }

    private fun userController(): UserController {
        val instance = userController

        return if (instance != null) {
            instance
        } else {
            val newInstance = UserController(
                require(gettingUser)
            )
            userController = newInstance
            newInstance
        }
    }

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
                    authenticationService().basic(credentials)
                }
            }
        }

        routing {
            authenticate("api-basic") {
                route("/api/v1") {
                    qualificationController().routes(this)
                    userController().routes(this)
                }
            }
        }
    }

    fun start() {
        embeddedServer(Netty, environment = applicationEngineEnvironment {

            module {
                moduleConfiguration()
            }

            connector {
                port = require(publicPort)
            }

        }).start(wait = true)
    }

    fun reset() {
        // external
        publicPort = null
        loggerFactory = null
        gettingUser = null
        gettingQualification = null
        addingQualification = null
        deletingQualification = null

        // internal services
        authenticationService = null

        // internal controller
        qualificationController = null
        userController = null
    }

    private inline fun <reified T : Any> require(value: T?): T =
        requireNotNull(value) { "RestModule has to be configured (missing ${T::class.qualifiedName})" }
}

@Suppress("unused") // receiver T is required to infer class to create logger for
internal inline fun <reified T> T.logger(): Logger {
    return RestModule.loggerFactory().invoke(T::class.java)
}