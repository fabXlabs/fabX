package cloud.fabX.fabXaccess.common.rest

import cloud.fabX.fabXaccess.RestApp
import cloud.fabX.fabXaccess.device.application.AddingDevice
import cloud.fabX.fabXaccess.device.application.AttachingTool
import cloud.fabX.fabXaccess.device.application.ChangingDevice
import cloud.fabX.fabXaccess.device.application.DeletingDevice
import cloud.fabX.fabXaccess.device.application.DetachingTool
import cloud.fabX.fabXaccess.device.application.GettingDevice
import cloud.fabX.fabXaccess.loggingModule
import cloud.fabX.fabXaccess.qualification.application.AddingQualification
import cloud.fabX.fabXaccess.qualification.application.ChangingQualification
import cloud.fabX.fabXaccess.qualification.application.DeletingQualification
import cloud.fabX.fabXaccess.qualification.application.GettingQualification
import cloud.fabX.fabXaccess.restModule
import cloud.fabX.fabXaccess.tool.application.AddingTool
import cloud.fabX.fabXaccess.tool.application.ChangingTool
import cloud.fabX.fabXaccess.tool.application.DeletingTool
import cloud.fabX.fabXaccess.tool.application.GettingTool
import cloud.fabX.fabXaccess.user.application.AddingUser
import cloud.fabX.fabXaccess.user.application.ChangingIsAdmin
import cloud.fabX.fabXaccess.user.application.ChangingUser
import cloud.fabX.fabXaccess.user.application.DeletingUser
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
        bindInstance { Mockito.mock(GettingUser::class.java) }
        bindInstance { Mockito.mock(AddingUser::class.java) }
        bindInstance { Mockito.mock(ChangingUser::class.java) }
        bindInstance { Mockito.mock(DeletingUser::class.java) }
        bindInstance { Mockito.mock(ChangingIsAdmin::class.java) }

        bindInstance { Mockito.mock(GettingQualification::class.java) }
        bindInstance { Mockito.mock(AddingQualification::class.java) }
        bindInstance { Mockito.mock(ChangingQualification::class.java) }
        bindInstance { Mockito.mock(DeletingQualification::class.java) }

        bindInstance { Mockito.mock(GettingTool::class.java) }
        bindInstance { Mockito.mock(AddingTool::class.java) }
        bindInstance { Mockito.mock(ChangingTool::class.java) }
        bindInstance { Mockito.mock(DeletingTool::class.java) }

        bindInstance { Mockito.mock(GettingDevice::class.java) }
        bindInstance { Mockito.mock(AddingDevice::class.java) }
        bindInstance { Mockito.mock(ChangingDevice::class.java) }
        bindInstance { Mockito.mock(DeletingDevice::class.java) }
        bindInstance { Mockito.mock(AttachingTool::class.java) }
        bindInstance { Mockito.mock(DetachingTool::class.java) }

        diSetup()
    }

    val restApp: RestApp by testApp.instance()

    runBlocking {
        withTestApplication(restApp.moduleConfiguration) {
            block()
        }
    }
}