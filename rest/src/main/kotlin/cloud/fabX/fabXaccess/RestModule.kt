package cloud.fabX.fabXaccess

import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.Logger
import io.ktor.application.call
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

object RestModule {
    private var loggerFactory: LoggerFactory? = null

    fun isFullyConfigured(): Boolean {
        return loggerFactory != null
    }

    fun configure(loggerFactory: LoggerFactory) {
        this.loggerFactory = loggerFactory
    }

    internal fun loggerFactory(): LoggerFactory {
        return require(loggerFactory)
    }

    fun start() {
        embeddedServer(Netty, environment = applicationEngineEnvironment {

            module {
                routing {
                    get("/") {
                        call.respondText("hello, world")
                    }
                }
            }

            connector {
                port = 8080
            }

        }).start(wait = true)
    }

    private fun <T : Any> require(value: T?): T = requireNotNull(value) { "RestModule has to be configured" }
}

@Suppress("unused") // receiver T is required to infer class to create logger for
internal inline fun <reified T> T.logger(): Logger {
    return RestModule.loggerFactory().invoke(T::class.java)
}