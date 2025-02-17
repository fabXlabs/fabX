package cloud.fabX.fabXaccess.common.rest

import cloud.fabX.fabXaccess.WebApp
import cloud.fabX.fabXaccess.common.application.domainSerializersModule
import cloud.fabX.fabXaccess.device.application.AddingCardIdentityAtDevice
import cloud.fabX.fabXaccess.device.application.AddingDevice
import cloud.fabX.fabXaccess.device.application.AttachingTool
import cloud.fabX.fabXaccess.device.application.ChangingDevice
import cloud.fabX.fabXaccess.device.application.ChangingFirmwareVersion
import cloud.fabX.fabXaccess.device.application.ChangingThumbnail
import cloud.fabX.fabXaccess.device.application.DeletingDevice
import cloud.fabX.fabXaccess.device.application.DetachingTool
import cloud.fabX.fabXaccess.device.application.GettingConfiguration
import cloud.fabX.fabXaccess.device.application.GettingDevice
import cloud.fabX.fabXaccess.device.application.GettingDevicePinStatus
import cloud.fabX.fabXaccess.device.application.RestartingDevice
import cloud.fabX.fabXaccess.device.application.UnlockingTool
import cloud.fabX.fabXaccess.device.application.UpdatingDeviceFirmware
import cloud.fabX.fabXaccess.device.model.GettingDeviceByIdentity
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
import cloud.fabX.fabXaccess.user.application.AddingPinIdentity
import cloud.fabX.fabXaccess.user.application.AddingUser
import cloud.fabX.fabXaccess.user.application.AddingUsernamePasswordIdentity
import cloud.fabX.fabXaccess.user.application.AddingWebauthnIdentity
import cloud.fabX.fabXaccess.user.application.ChangingIsAdmin
import cloud.fabX.fabXaccess.user.application.ChangingPassword
import cloud.fabX.fabXaccess.user.application.ChangingUser
import cloud.fabX.fabXaccess.user.application.DeletingUser
import cloud.fabX.fabXaccess.user.application.GettingSoftDeletedUsers
import cloud.fabX.fabXaccess.user.application.GettingUser
import cloud.fabX.fabXaccess.user.application.GettingUserByIdentity
import cloud.fabX.fabXaccess.user.application.GettingUserIdByWikiName
import cloud.fabX.fabXaccess.user.application.GettingUserSourcingEvents
import cloud.fabX.fabXaccess.user.application.LoggingUnlockedTool
import cloud.fabX.fabXaccess.user.application.RemovingCardIdentity
import cloud.fabX.fabXaccess.user.application.RemovingInstructorQualification
import cloud.fabX.fabXaccess.user.application.RemovingMemberQualification
import cloud.fabX.fabXaccess.user.application.RemovingPhoneNrIdentity
import cloud.fabX.fabXaccess.user.application.RemovingPinIdentity
import cloud.fabX.fabXaccess.user.application.RemovingUsernamePasswordIdentity
import cloud.fabX.fabXaccess.user.application.RemovingWebauthnIdentity
import cloud.fabX.fabXaccess.user.application.UserMetrics
import cloud.fabX.fabXaccess.user.model.GettingUserById
import cloud.fabX.fabXaccess.user.model.GettingUserByUsername
import cloud.fabX.fabXaccess.user.model.WebauthnRepository
import cloud.fabX.fabXaccess.webModule
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import java.io.File
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.bindConstant
import org.kodein.di.bindInstance
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import org.mockito.Mockito
import cloud.fabX.fabXaccess.tool.application.ChangingThumbnail as ChangingToolThumbnail

internal fun withTestApp(
    diSetup: DI.MainBuilder.() -> Unit,
    block: suspend ApplicationTestBuilder.() -> Unit
) {
    withTestApp(diSetup, {}, block)
}

internal fun withTestApp(
    diSetup: DI.MainBuilder.() -> Unit,
    diGetter: (DI) -> Unit = {},
    block: suspend ApplicationTestBuilder.() -> Unit
) {
    val testApp = DI {
        import(webModule)
        import(loggingModule)

        bindConstant(tag = "port") { -1 }
        bindConstant(tag = "deviceReceiveTimeoutMillis") { 1000L }

        bindConstant(tag = "jwtIssuer") { "http://localhost/" }
        bindConstant(tag = "jwtAudience") { "http://localhost/" }
        bindConstant(tag = "jwtHMAC256Secret") { "secret123" }

        bindConstant(tag = "cookieDomain") { "localhost" }
        bindConstant(tag = "cookiePath") { "/" }

        bindConstant(tag = "corsHost") { "" }

        bindSingleton<Clock> { Clock.System }

        bindInstance { Mockito.mock(GettingUserById::class.java) }
        bindInstance { Mockito.mock(GettingUserByIdentity::class.java) }
        bindInstance { Mockito.mock(GettingUserByUsername::class.java) }
        bindInstance { Mockito.mock(GettingUserIdByWikiName::class.java) }
        bindInstance { Mockito.mock(GettingUserSourcingEvents::class.java) }
        bindInstance { Mockito.mock(GettingUser::class.java) }
        bindInstance { Mockito.mock(AddingUser::class.java) }
        bindInstance { Mockito.mock(ChangingUser::class.java) }
        bindInstance { Mockito.mock(DeletingUser::class.java) }
        bindInstance { Mockito.mock(ChangingIsAdmin::class.java) }
        bindInstance { Mockito.mock(GettingSoftDeletedUsers::class.java) }
        bindInstance { Mockito.mock(AddingInstructorQualification::class.java) }
        bindInstance { Mockito.mock(RemovingInstructorQualification::class.java) }
        bindInstance { Mockito.mock(AddingMemberQualification::class.java) }
        bindInstance { Mockito.mock(RemovingMemberQualification::class.java) }
        bindInstance { Mockito.mock(AddingUsernamePasswordIdentity::class.java) }
        bindInstance { Mockito.mock(ChangingPassword::class.java) }
        bindInstance { Mockito.mock(RemovingUsernamePasswordIdentity::class.java) }
        bindInstance { Mockito.mock(AddingWebauthnIdentity::class.java) }
        bindInstance { Mockito.mock(RemovingWebauthnIdentity::class.java) }
        bindInstance { Mockito.mock(AddingCardIdentity::class.java) }
        bindInstance { Mockito.mock(RemovingCardIdentity::class.java) }
        bindInstance { Mockito.mock(AddingPhoneNrIdentity::class.java) }
        bindInstance { Mockito.mock(RemovingPhoneNrIdentity::class.java) }
        bindInstance { Mockito.mock(AddingPinIdentity::class.java) }
        bindInstance { Mockito.mock(RemovingPinIdentity::class.java) }
        bindInstance { Mockito.mock(LoggingUnlockedTool::class.java) }

        bindInstance { Mockito.mock(GettingQualification::class.java) }
        bindInstance { Mockito.mock(AddingQualification::class.java) }
        bindInstance { Mockito.mock(ChangingQualification::class.java) }
        bindInstance { Mockito.mock(DeletingQualification::class.java) }

        bindInstance { Mockito.mock(GettingTool::class.java) }
        bindInstance { Mockito.mock(AddingTool::class.java) }
        bindInstance { Mockito.mock(ChangingTool::class.java) }
        bindInstance { Mockito.mock(ChangingToolThumbnail::class.java) }
        bindInstance { Mockito.mock(DeletingTool::class.java) }

        bindInstance { Mockito.mock(GettingDevice::class.java) }
        bindInstance { Mockito.mock(GettingDeviceByIdentity::class.java) }
        bindInstance { Mockito.mock(AddingDevice::class.java) }
        bindInstance { Mockito.mock(ChangingDevice::class.java) }
        bindInstance { Mockito.mock(ChangingThumbnail::class.java) }
        bindInstance { Mockito.mock(ChangingFirmwareVersion::class.java) }
        bindInstance { Mockito.mock(DeletingDevice::class.java) }
        bindInstance { Mockito.mock(AttachingTool::class.java) }
        bindInstance { Mockito.mock(DetachingTool::class.java) }
        bindInstance { Mockito.mock(GettingConfiguration::class.java) }
        bindInstance { Mockito.mock(UnlockingTool::class.java) }
        bindInstance { Mockito.mock(RestartingDevice::class.java) }
        bindInstance { Mockito.mock(UpdatingDeviceFirmware::class.java) }
        bindInstance { Mockito.mock(AddingCardIdentityAtDevice::class.java) }
        bindInstance { Mockito.mock(DeviceCommandHandler::class.java) }
        bindInstance { Mockito.mock(DeviceNotificationHandler::class.java) }
        bindInstance { Mockito.mock(GettingDevicePinStatus::class.java) }

        bindInstance { Mockito.mock(UserMetrics::class.java) }
        bindInstance { Mockito.mock(WebauthnRepository::class.java) }

        bindConstant(tag = "webauthnOrigin") { "http://localhost/" }
        bindConstant(tag = "webauthnRpId") { "localhost" }
        bindConstant(tag = "webauthnRpName") { "fabX" }

        bindConstant(tag = "firmwareDirectory") { File("/tmp/fabXtest") }

        bindInstance(tag = "metricsPassword") { "supersecretmetricspassword" }

        bindConstant(tag = "httpsRedirect") { false }

        diSetup()
    }

    val webApp: WebApp by testApp.instance()

    diGetter(testApp)

    testApplication {
        application {
            webApp.moduleConfiguration(this)
        }
        block()
    }
}

internal fun ApplicationTestBuilder.c(): HttpClient {
    return createClient {
        install(ContentNegotiation) {
            json(Json {
                serializersModule = domainSerializersModule
            })
        }
        install(WebSockets)
        defaultRequest {
            header(HttpHeaders.XForwardedProto, "https")
        }
    }
}