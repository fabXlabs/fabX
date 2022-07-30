package cloud.fabX.fabXaccess

import cloud.fabX.fabXaccess.common.LiquibaseMigrationHandler
import cloud.fabX.fabXaccess.device.infrastructure.DeviceDatabaseRepository
import cloud.fabX.fabXaccess.qualification.infrastructure.QualificationDatabaseRepository
import cloud.fabX.fabXaccess.tool.infrastructure.ToolDatabaseRepository
import cloud.fabX.fabXaccess.user.infrastructure.UserDatabaseRepository
import org.jetbrains.exposed.sql.Database
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance

// TODO database operations on Dispatchers.IO thread pool

val persistenceModule = DI.Module("persistence") {
    bindSingleton { DeviceDatabaseRepository(instance()) }
    bindSingleton { QualificationDatabaseRepository(instance()) }
    bindSingleton { ToolDatabaseRepository(instance(), instance()) }
    bindSingleton { UserDatabaseRepository(instance()) }

    bindSingleton {
        LiquibaseMigrationHandler(
            url = instance(tag = "dburl"),
            user = instance(tag = "dbuser"),
            password = instance(tag = "dbpassword")
        )
    }

    bindSingleton {
        PersistenceApp(
            instance(),
            instance()
        )
    }

    bindSingleton {
        Database.connect(
            instance(tag = "dburl"),
            driver = instance(tag = "dbdriver"),
            user = instance(tag = "dbuser"),
            password = instance(tag = "dbpassword")
        )
    }
}