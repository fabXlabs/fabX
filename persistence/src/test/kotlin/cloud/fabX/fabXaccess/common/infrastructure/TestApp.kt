package cloud.fabX.fabXaccess.common.infrastructure

import cloud.fabX.fabXaccess.PersistenceApp
import cloud.fabX.fabXaccess.device.infrastructure.DeviceSourcingEventDAO
import cloud.fabX.fabXaccess.loggingModule
import cloud.fabX.fabXaccess.persistenceModule
import cloud.fabX.fabXaccess.qualification.infrastructure.QualificationSourcingEventDAO
import cloud.fabX.fabXaccess.tool.infrastructure.ToolSourcingEventDAO
import cloud.fabX.fabXaccess.user.infrastructure.UserSourcingEventDAO
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import org.kodein.di.bindInstance
import org.kodein.di.instance
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

val postgresImageName = DockerImageName.parse("postgres").withTag("13")
val postgresContainer = PostgreSQLContainer(postgresImageName)

var initialised = false

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
        bindInstance(tag = "dbdriver") { "org.postgresql.Driver" }
        bindInstance(tag = "dbuser") { postgresContainer.username }
        bindInstance(tag = "dbpassword") { postgresContainer.password }
    }

    // only initialise database once
    if (!initialised) {
        val persistenceApp: PersistenceApp by testApp.instance()
        persistenceApp.initialise()
        initialised = true
    }

    val db: Database by testApp.instance()

    transaction(db) {
        QualificationSourcingEventDAO.deleteAll()
        DeviceSourcingEventDAO.deleteAll()
        ToolSourcingEventDAO.deleteAll()
        UserSourcingEventDAO.deleteAll()
    }

    runBlocking {
        block(testApp)
    }
}