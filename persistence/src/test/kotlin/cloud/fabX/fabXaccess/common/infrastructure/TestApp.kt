package cloud.fabX.fabXaccess.common.infrastructure

import cloud.fabX.fabXaccess.PersistenceApp
import cloud.fabX.fabXaccess.loggingModule
import cloud.fabX.fabXaccess.persistenceModule
import cloud.fabX.fabXaccess.qualification.infrastructure.QualificationDatabaseRepository
import cloud.fabX.fabXaccess.tool.infrastructure.ToolDatabaseRepository
import cloud.fabX.fabXaccess.user.infrastructure.UserDatabaseRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import org.kodein.di.bindInstance
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

val postgresImageName = DockerImageName.parse("postgres").withTag("13")
val postgresContainer = PostgreSQLContainer(postgresImageName)
    .withCommand(
        "-c", "fsync=off",
        "-c", "synchronous_commit=off",
        "-c", "full_page_writes=off",
    )

var initialised = false

@OptIn(ExperimentalCoroutinesApi::class)
internal fun withTestApp(
    block: suspend (DI) -> Unit
) {
    if (!postgresContainer.isRunning) {
        println("starting postgres container...")
        postgresContainer.start()
        println("...started postgres container")
    }

    val testApp = DI {
        import(persistenceModule)
        import(loggingModule)

        bindInstance(tag = "dburl") { postgresContainer.jdbcUrl }
        bindInstance(tag = "dbuser") { postgresContainer.username }
        bindInstance(tag = "dbpassword") { postgresContainer.password }

        // additionally bind non-cached variants for direct tests
        bindSingleton { QualificationDatabaseRepository(instance()) }
        bindSingleton { ToolDatabaseRepository(instance(), instance()) }
        bindSingleton { UserDatabaseRepository(instance()) }
    }

    // only initialise database once
    if (!initialised) {
        val persistenceApp: PersistenceApp by testApp.instance()
        persistenceApp.initialise()
        initialised = true
    }

    val db: Database by testApp.instance()

    transaction(db) {
        exec("TRUNCATE TABLE QualificationSourcingEvent, DeviceSourcingEvent, ToolSourcingEvent, UserSourcingEvent")
    }

    runTest {
        block(testApp)
    }
}