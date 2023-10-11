package cloud.fabX.fabXaccess.common

import java.sql.DriverManager
import liquibase.command.CommandScope
import liquibase.command.core.UpdateCommandStep
import liquibase.command.core.helpers.DbUrlConnectionCommandStep
import liquibase.database.Database
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection

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

        CommandScope(UpdateCommandStep.COMMAND_NAME[0])
            .addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, CHANGELOG_FILE)
            .addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, database)
            .execute()

        database.close()
    }
}