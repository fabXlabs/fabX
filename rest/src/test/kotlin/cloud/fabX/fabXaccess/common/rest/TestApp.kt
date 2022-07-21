package cloud.fabX.fabXaccess.common.rest

import cloud.fabX.fabXaccess.RestModule
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.withTestApplication
import kotlinx.coroutines.runBlocking

internal fun withTestApp(
    block: TestApplicationEngine.() -> Unit
) {
    runBlocking {
        withTestApplication(RestModule.moduleConfiguration) {
            block()
        }
    }
}
