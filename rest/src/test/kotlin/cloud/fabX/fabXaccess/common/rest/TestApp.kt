package cloud.fabX.fabXaccess.common.rest

import cloud.fabX.fabXaccess.RestApp
import cloud.fabX.fabXaccess.logging.LogbackLoggerFactory
import cloud.fabX.fabXaccess.qualification.application.AddingQualification
import cloud.fabX.fabXaccess.qualification.application.DeletingQualification
import cloud.fabX.fabXaccess.qualification.application.GettingQualification
import cloud.fabX.fabXaccess.user.application.GettingUser
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.withTestApplication
import kotlinx.coroutines.runBlocking
import org.mockito.Mockito

internal fun withTestApp(
    block: TestApplicationEngine.() -> Unit
) {
    RestApp.configureLoggerFactory(LogbackLoggerFactory())

    runBlocking {
        withTestApplication(RestApp.moduleConfiguration) {
            block()
        }
    }
}

internal fun mockAll() {
    RestApp.reset()

    RestApp.configureGettingUser(Mockito.mock(GettingUser::class.java))

    RestApp.configureGettingQualification(Mockito.mock(GettingQualification::class.java))
    RestApp.configureAddingQualification(Mockito.mock(AddingQualification::class.java))
    RestApp.configureDeletingQualification(Mockito.mock(DeletingQualification::class.java))
}