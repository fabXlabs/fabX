package cloud.fabX.fabXaccess

import cloud.fabX.fabXaccess.device.rest.DeviceController
import cloud.fabX.fabXaccess.device.ws.DeviceCommandHandlerImpl
import cloud.fabX.fabXaccess.device.ws.DeviceNotificationHandlerImpl
import cloud.fabX.fabXaccess.device.ws.DeviceWebsocketController
import cloud.fabX.fabXaccess.device.ws.UnlockToolAtDeviceViaWebsocket
import cloud.fabX.fabXaccess.qualification.rest.QualificationController
import cloud.fabX.fabXaccess.tool.rest.ToolController
import cloud.fabX.fabXaccess.user.rest.AuthenticationService
import cloud.fabX.fabXaccess.user.rest.LoginController
import cloud.fabX.fabXaccess.user.rest.UserController
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance

val webModule = DI.Module("web") {
    bindSingleton { AuthenticationService(instance(), instance(), instance()) }

    bindSingleton { DeviceCommandHandlerImpl(instance(), instance(), instance()) }
    bindSingleton { DeviceNotificationHandlerImpl(instance(), instance()) }

    bindSingleton { QualificationController(instance(), instance(), instance(), instance()) }
    bindSingleton { ToolController(instance(), instance(), instance(), instance()) }
    bindSingleton {
        DeviceController(
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance()
        )
    }
    bindSingleton {
        DeviceWebsocketController(
            instance(),
            instance(),
            instance(),
            instance(tag = "deviceReceiveTimeoutMillis")
        )
    }
    bindSingleton {
        UserController(
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance()
        )
    }
    bindSingleton {
        LoginController(
            instance(),
            instance(tag = "jwtIssuer"),
            instance(tag = "jwtAudience"),
            instance(tag = "jwtHMAC256Secret")
        )
    }

    bindSingleton { UnlockToolAtDeviceViaWebsocket(instance()) }

    bindSingleton {
        WebApp(
            instance(),
            instance(tag = "port"),
            instance(tag = "jwtIssuer"),
            instance(tag = "jwtAudience"),
            instance(tag = "jwtHMAC256Secret"),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance()
        )
    }
}