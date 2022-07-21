package cloud.fabX.fabXaccess.common.rest

import cloud.fabX.fabXaccess.RestModule
import cloud.fabX.fabXaccess.logging.LogbackLoggerFactory
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.withTestApplication
import kotlinx.coroutines.runBlocking

internal fun withTestApp(
    block: TestApplicationEngine.() -> Unit
) {
    RestModule.configureLoggerFactory(LogbackLoggerFactory())

    runBlocking {
        withTestApplication(RestModule.moduleConfiguration) {
            block()
        }
    }
}
