package cloud.fabX.fabXaccess.common.infrastructure

import cloud.fabX.fabXaccess.device.infrastructure.DeviceSourcingEventDAO
import cloud.fabX.fabXaccess.persistenceModule
import cloud.fabX.fabXaccess.qualification.infrastructure.QualificationSourcingEventDAO
import cloud.fabX.fabXaccess.tool.infrastructure.ToolSourcingEventDAO
import cloud.fabX.fabXaccess.user.infrastructure.UserSourcingEventDAO
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import org.kodein.di.instance

internal fun withTestApp(
    diSetup: DI.MainBuilder.() -> Unit,
    block: (DI) -> Unit
) {
    val testApp = DI {
        import(persistenceModule)
        diSetup()
    }

    val db: Database by testApp.instance()

    transaction(db) {
        addLogger(StdOutSqlLogger)

        // TODO database migration tool
        SchemaUtils.createMissingTablesAndColumns(QualificationSourcingEventDAO)
        SchemaUtils.createMissingTablesAndColumns(DeviceSourcingEventDAO)
        SchemaUtils.createMissingTablesAndColumns(ToolSourcingEventDAO)
        SchemaUtils.createMissingTablesAndColumns(UserSourcingEventDAO)

        QualificationSourcingEventDAO.deleteAll()
        DeviceSourcingEventDAO.deleteAll()
        ToolSourcingEventDAO.deleteAll()
        UserSourcingEventDAO.deleteAll()
    }

    block(testApp)
}