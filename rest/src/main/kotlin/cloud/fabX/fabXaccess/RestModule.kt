package cloud.fabX.fabXaccess

import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.model.newUserId
import cloud.fabX.fabXaccess.qualification.rest.QualificationController
import cloud.fabX.fabXaccess.qualification.application.GettingQualification
import cloud.fabX.fabXaccess.user.model.Admin
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
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
    private var gettingQualification: GettingQualification? = null

    // controller
    private var qualificationController: QualificationController? = null

    // TODO extract authentication from request
    internal val fakeActor = Admin(newUserId(), "some.admin")

    fun isFullyConfigured(): Boolean {
        return try {
            require(publicPort)
            require(loggerFactory)
            require(gettingQualification)

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

    fun configureGettingQualification(gettingQualification: GettingQualification) {
        this.gettingQualification = gettingQualification
    }

    internal fun loggerFactory(): LoggerFactory {
        return require(loggerFactory)
    }

    internal fun qualificationController(): QualificationController {
        val instance = qualificationController

        return if (instance != null) {
            instance
        } else {
            val newInstance = QualificationController(require(gettingQualification))
            qualificationController = newInstance
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

        routing {
            route("/api/v1") {
                qualificationController().routes(this)
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
        publicPort = null
        loggerFactory = null
        gettingQualification = null
        qualificationController = null
    }

    private inline fun <reified T : Any> require(value: T?): T =
        requireNotNull(value) { "DomainModule has to be configured (missing ${T::class.qualifiedName})" }
}

@Suppress("unused") // receiver T is required to infer class to create logger for
internal inline fun <reified T> T.logger(): Logger {
    return RestModule.loggerFactory().invoke(T::class.java)
}