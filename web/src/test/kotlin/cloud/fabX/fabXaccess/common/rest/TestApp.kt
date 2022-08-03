package cloud.fabX.fabXaccess.common.rest

import cloud.fabX.fabXaccess.WebApp
import cloud.fabX.fabXaccess.device.application.AddingDevice
import cloud.fabX.fabXaccess.device.application.AttachingTool
import cloud.fabX.fabXaccess.device.application.ChangingDevice
import cloud.fabX.fabXaccess.device.application.DeletingDevice
import cloud.fabX.fabXaccess.device.application.DetachingTool
import cloud.fabX.fabXaccess.device.application.GettingConfiguration
import cloud.fabX.fabXaccess.device.application.GettingDevice
import cloud.fabX.fabXaccess.device.application.UnlockingTool
import cloud.fabX.fabXaccess.device.ws.DeviceCommandHandler
import cloud.fabX.fabXaccess.device.ws.DeviceNotificationHandler
import cloud.fabX.fabXaccess.loggingModule
import cloud.fabX.fabXaccess.qualification.application.AddingQualification
import cloud.fabX.fabXaccess.qualification.application.ChangingQualification
import cloud.fabX.fabXaccess.qualification.application.DeletingQualification
import cloud.fabX.fabXaccess.qualification.application.GettingQualification
import cloud.fabX.fabXaccess.tool.application.AddingTool
import cloud.fabX.fabXaccess.tool.application.ChangingTool
import cloud.fabX.fabXaccess.tool.application.DeletingTool
import cloud.fabX.fabXaccess.tool.application.GettingTool
import cloud.fabX.fabXaccess.user.application.AddingCardIdentity
import cloud.fabX.fabXaccess.user.application.AddingInstructorQualification
import cloud.fabX.fabXaccess.user.application.AddingMemberQualification
import cloud.fabX.fabXaccess.user.application.AddingPhoneNrIdentity
import cloud.fabX.fabXaccess.user.application.AddingUser
import cloud.fabX.fabXaccess.user.application.AddingUsernamePasswordIdentity
import cloud.fabX.fabXaccess.user.application.ChangingIsAdmin
import cloud.fabX.fabXaccess.user.application.ChangingUser
import cloud.fabX.fabXaccess.user.application.DeletingUser
import cloud.fabX.fabXaccess.user.application.GettingUser
import cloud.fabX.fabXaccess.user.application.GettingUserByIdentity
import cloud.fabX.fabXaccess.user.application.LoggingUnlockedTool
import cloud.fabX.fabXaccess.user.application.RemovingCardIdentity
import cloud.fabX.fabXaccess.user.application.RemovingInstructorQualification
import cloud.fabX.fabXaccess.user.application.RemovingMemberQualification
import cloud.fabX.fabXaccess.user.application.RemovingPhoneNrIdentity
import cloud.fabX.fabXaccess.user.application.RemovingUsernamePasswordIdentity
import cloud.fabX.fabXaccess.webModule
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.withTestApplication
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.kodein.di.DI
import org.kodein.di.bindConstant
import org.kodein.di.bindInstance
import org.kodein.di.instance
import org.mockito.Mockito

@OptIn(ExperimentalCoroutinesApi::class)

internal fun withTestApp(
    diSetup: DI.MainBuilder.() -> Unit,
    block: suspend TestApplicationEngine.() -> Unit
) {
    withTestApp(diSetup, {}, block)
}

@OptIn(ExperimentalCoroutinesApi::class)
internal fun withTestApp(
    diSetup: DI.MainBuilder.() -> Unit,
    diGetter: (DI) -> Unit = {},
    block: suspend TestApplicationEngine.() -> Unit
) {
    val testApp = DI {
        import(webModule)
        import(loggingModule)

        bindConstant(tag = "port") { -1 }
        bindConstant(tag = "deviceReceiveTimeoutMillis") { 1000L }

        bindInstance { Mockito.mock(GettingUserByIdentity::class.java) }
        bindInstance { Mockito.mock(GettingUser::class.java) }
        bindInstance { Mockito.mock(AddingUser::class.java) }
        bindInstance { Mockito.mock(ChangingUser::class.java) }
        bindInstance { Mockito.mock(DeletingUser::class.java) }
        bindInstance { Mockito.mock(ChangingIsAdmin::class.java) }
        bindInstance { Mockito.mock(AddingInstructorQualification::class.java) }
        bindInstance { Mockito.mock(RemovingInstructorQualification::class.java) }
        bindInstance { Mockito.mock(AddingMemberQualification::class.java) }
        bindInstance { Mockito.mock(RemovingMemberQualification::class.java) }
        bindInstance { Mockito.mock(AddingUsernamePasswordIdentity::class.java) }
        bindInstance { Mockito.mock(RemovingUsernamePasswordIdentity::class.java) }
        bindInstance { Mockito.mock(AddingCardIdentity::class.java) }
        bindInstance { Mockito.mock(RemovingCardIdentity::class.java) }
        bindInstance { Mockito.mock(AddingPhoneNrIdentity::class.java) }
        bindInstance { Mockito.mock(RemovingPhoneNrIdentity::class.java) }
        bindInstance { Mockito.mock(LoggingUnlockedTool::class.java) }

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
        bindInstance { Mockito.mock(GettingConfiguration::class.java) }
        bindInstance { Mockito.mock(UnlockingTool::class.java) }
        bindInstance { Mockito.mock(DeviceCommandHandler::class.java) }
        bindInstance { Mockito.mock(DeviceNotificationHandler::class.java) }

        diSetup()
    }

    val webApp: WebApp by testApp.instance()

    diGetter(testApp)

    // TODO `withTestApplication` is deprecated. Please use new `testApplication` API
    withTestApplication(webApp.moduleConfiguration) {
        // TODO with ktor 2: refactor testing according design described here
        //      https://youtrack.jetbrains.com/issue/KTOR-971
        //      -> should enable virtual time for coroutines of test application
        runTest {
            block()
        }
    }
}