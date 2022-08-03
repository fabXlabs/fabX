package cloud.fabX.fabXaccess.common

import java.sql.DriverManager
import liquibase.Contexts
import liquibase.LabelExpression
import liquibase.Liquibase
import liquibase.database.Database
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor

class LiquibaseMigrationHandler(
    private val url: String,
    private val user: String,
    private val password: String
) {
    companion object {
        const val CHANGELOG_FILE = "changelog/000-root.xml"
    }

    private fun openConnection(): Database {
        val connection = DriverManager.getConnection(url, user, password)

        return DatabaseFactory.getInstance().findCorrectDatabaseImplementation(JdbcConnection(connection))
    }

    internal fun update() {
        val database = openConnection()
        val liquibase = Liquibase(CHANGELOG_FILE, ClassLoaderResourceAccessor(), database)
        liquibase.update(Contexts(), LabelExpression())
    }
}