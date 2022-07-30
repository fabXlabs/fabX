package cloud.fabX.fabXaccess

import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.device.infrastructure.DeviceSourcingEventDAO
import cloud.fabX.fabXaccess.qualification.infrastructure.QualificationSourcingEventDAO
import cloud.fabX.fabXaccess.tool.infrastructure.ToolSourcingEventDAO
import cloud.fabX.fabXaccess.user.infrastructure.UserSourcingEventDAO
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

class PersistenceApp(
    loggerFactory: LoggerFactory,
    private val db: Database,
    private val url: String,
    private val driver: String,
    private val user: String,
    private val password: String
) {
    private val log: Logger = loggerFactory.invoke(this::class.java)

    fun initialise() {
        log.debug("initialising PersistenceApp...")

        transaction(db) {
            addLogger(StdOutSqlLogger)

            // TODO database migration tool
            SchemaUtils.createMissingTablesAndColumns(QualificationSourcingEventDAO)
            SchemaUtils.createMissingTablesAndColumns(DeviceSourcingEventDAO)
            SchemaUtils.createMissingTablesAndColumns(ToolSourcingEventDAO)
            SchemaUtils.createMissingTablesAndColumns(UserSourcingEventDAO)
        }
    }
}