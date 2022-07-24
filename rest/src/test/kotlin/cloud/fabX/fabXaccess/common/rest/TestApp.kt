package cloud.fabX.fabXaccess.common.rest

import cloud.fabX.fabXaccess.RestModule
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
    RestModule.configureLoggerFactory(LogbackLoggerFactory())

    runBlocking {
        withTestApplication(RestModule.moduleConfiguration) {
            block()
        }
    }
}

internal fun mockAll() {
    RestModule.reset()

    RestModule.configureGettingUser(Mockito.mock(GettingUser::class.java))

    RestModule.configureGettingQualification(Mockito.mock(GettingQualification::class.java))
    RestModule.configureAddingQualification(Mockito.mock(AddingQualification::class.java))
    RestModule.configureDeletingQualification(Mockito.mock(DeletingQualification::class.java))
}