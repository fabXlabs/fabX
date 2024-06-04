package cloud.fabX.fabXaccess

import cloud.fabX.fabXaccess.common.LiquibaseMigrationHandler
import cloud.fabX.fabXaccess.device.infrastructure.DeviceDatabaseRepository
import cloud.fabX.fabXaccess.device.infrastructure.DevicePinStatusInMemoryRepository
import cloud.fabX.fabXaccess.qualification.infrastructure.QualificationDatabaseRepository
import cloud.fabX.fabXaccess.tool.infrastructure.ToolDatabaseRepository
import cloud.fabX.fabXaccess.user.infrastructure.UserDatabaseRepository
import cloud.fabX.fabXaccess.user.infrastructure.WebauthnInMemoryRepository
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance

val persistenceModule = makePersistenceModule()

fun makePersistenceModule(): DI.Module = DI.Module("persistence") {

    bindSingleton { DeviceDatabaseRepository(instance()) }
    bindSingleton { QualificationDatabaseRepository(instance()) }
    bindSingleton { ToolDatabaseRepository(instance(), instance()) }
    bindSingleton { UserDatabaseRepository(instance()) }

    bindSingleton { WebauthnInMemoryRepository() }

    bindSingleton { DevicePinStatusInMemoryRepository() }

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
        val config = HikariConfig()
        config.jdbcUrl = instance(tag = "dburl")
        config.username = instance(tag = "dbuser")
        config.password = instance(tag = "dbpassword")
        config.driverClassName = "org.postgresql.Driver"

        HikariDataSource(config)
    }

    bindSingleton {
        Database.connect(instance<HikariDataSource>())
    }
}
