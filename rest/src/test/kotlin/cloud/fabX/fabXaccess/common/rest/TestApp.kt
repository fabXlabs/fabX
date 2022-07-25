package cloud.fabX.fabXaccess.common.rest

import cloud.fabX.fabXaccess.RestApp
import cloud.fabX.fabXaccess.loggingModule
import cloud.fabX.fabXaccess.qualification.application.AddingQualification
import cloud.fabX.fabXaccess.qualification.application.DeletingQualification
import cloud.fabX.fabXaccess.qualification.application.GettingQualification
import cloud.fabX.fabXaccess.restModule
import cloud.fabX.fabXaccess.user.application.GettingUser
import cloud.fabX.fabXaccess.user.application.GettingUserByIdentity
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.withTestApplication
import kotlinx.coroutines.runBlocking
import org.kodein.di.DI
import org.kodein.di.bindConstant
import org.kodein.di.bindInstance
import org.kodein.di.instance
import org.mockito.Mockito

internal fun withTestApp(
    diSetup: DI.MainBuilder.() -> Unit,
    block: TestApplicationEngine.() -> Unit
) {
    val testApp = DI {
        import(restModule)
        import(loggingModule)

        bindConstant(tag = "port") { -1 }

        bindInstance { Mockito.mock(GettingUserByIdentity::class.java) }
        bindInstance { Mockito.mock(GettingQualification::class.java) }
        bindInstance { Mockito.mock(AddingQualification::class.java) }
        bindInstance { Mockito.mock(DeletingQualification::class.java) }
        bindInstance { Mockito.mock(GettingUser::class.java) }

        diSetup()
    }

    val restApp: RestApp by testApp.instance()

    runBlocking {
        withTestApplication(restApp.moduleConfiguration) {
            block()
        }
    }
}