package cloud.fabX.fabXaccess.common.infrastructure

import cloud.fabX.fabXaccess.persistenceModule
import cloud.fabX.fabXaccess.qualification.infrastructure.QualificationSourcingEventDAO
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
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
        // TODO database migration tool
        SchemaUtils.createMissingTablesAndColumns(QualificationSourcingEventDAO)

        QualificationSourcingEventDAO.deleteAll()
    }

    block(testApp)
}