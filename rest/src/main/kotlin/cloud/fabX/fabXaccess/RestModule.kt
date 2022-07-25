package cloud.fabX.fabXaccess

import cloud.fabX.fabXaccess.qualification.rest.QualificationController
import cloud.fabX.fabXaccess.user.rest.AuthenticationService
import cloud.fabX.fabXaccess.user.rest.UserController
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance

val restModule = DI.Module("rest") {
    bindSingleton { AuthenticationService(instance()) }

    bindSingleton { QualificationController(instance(), instance(), instance(), instance()) }
    bindSingleton { UserController(instance()) }

    bindSingleton { RestApp(instance(), instance(tag = "port"), instance(), instance(), instance()) }
}