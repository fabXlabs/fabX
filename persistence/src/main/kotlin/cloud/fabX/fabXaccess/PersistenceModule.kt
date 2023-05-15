package cloud.fabX.fabXaccess

import cloud.fabX.fabXaccess.common.LiquibaseMigrationHandler
import cloud.fabX.fabXaccess.device.infrastructure.CachedDeviceDatabaseRepository
import cloud.fabX.fabXaccess.qualification.infrastructure.CachedQualificationDatabaseRepository
import cloud.fabX.fabXaccess.tool.infrastructure.CachedToolDatabaseRepository
import cloud.fabX.fabXaccess.user.infrastructure.CachedUserDatabaseRepository
import cloud.fabX.fabXaccess.user.infrastructure.WebauthnInMemoryRepository
import org.jetbrains.exposed.sql.Database
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance

val persistenceModule = DI.Module("persistence") {
    bindSingleton { CachedDeviceDatabaseRepository(instance()) }
    bindSingleton { CachedQualificationDatabaseRepository(instance()) }
    bindSingleton { CachedToolDatabaseRepository(instance(), instance()) }
    bindSingleton { CachedUserDatabaseRepository(instance()) }
    bindSingleton { WebauthnInMemoryRepository() }

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
            user = instance(tag = "dbuser"),
            password = instance(tag = "dbpassword"),
            driver = "org.postgresql.Driver"
        )
    }
}