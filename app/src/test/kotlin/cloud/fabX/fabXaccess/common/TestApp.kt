package cloud.fabX.fabXaccess.common

import cloud.fabX.fabXaccess.AppConfiguration
import cloud.fabX.fabXaccess.RestModule
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.withTestApplication
import kotlinx.coroutines.runBlocking

internal fun withTestApp(
    block: TestApplicationEngine.() -> Unit
) {
    AppConfiguration.configure()

    runBlocking {
        withTestApplication(RestModule.moduleConfiguration) {
            block()
        }
    }
}