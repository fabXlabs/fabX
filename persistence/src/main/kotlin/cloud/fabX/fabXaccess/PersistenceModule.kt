package cloud.fabX.fabXaccess

import cloud.fabX.fabXaccess.device.infrastructure.DeviceDatabaseRepository
import cloud.fabX.fabXaccess.qualification.infrastructure.QualificationDatabaseRepository
import cloud.fabX.fabXaccess.tool.infrastructure.ToolDatabaseRepository
import cloud.fabX.fabXaccess.user.infrastructure.UserDatabaseRepository
import org.kodein.di.DI
import org.kodein.di.bindSingleton

val persistenceModule = DI.Module("persistence") {
    bindSingleton { DeviceDatabaseRepository() }
    bindSingleton { QualificationDatabaseRepository() }
    bindSingleton { ToolDatabaseRepository() }
    bindSingleton { UserDatabaseRepository() }
}