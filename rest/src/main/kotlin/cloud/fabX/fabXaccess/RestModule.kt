package cloud.fabX.fabXaccess

import cloud.fabX.fabXaccess.device.rest.DeviceController
import cloud.fabX.fabXaccess.qualification.rest.QualificationController
import cloud.fabX.fabXaccess.tool.rest.ToolController
import cloud.fabX.fabXaccess.user.rest.AuthenticationService
import cloud.fabX.fabXaccess.user.rest.UserController
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance

val restModule = DI.Module("rest") {
    bindSingleton { AuthenticationService(instance()) }

    bindSingleton { QualificationController(instance(), instance(), instance(), instance()) }
    bindSingleton { ToolController(instance(), instance(), instance(), instance()) }
    bindSingleton { DeviceController(instance(), instance(), instance(), instance(), instance(), instance()) }
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
            instance()
        )
    }

    bindSingleton {
        RestApp(
            instance(),
            instance(tag = "port"),
            instance(),
            instance(),
            instance(),
            instance(),
            instance()
        )
    }
}