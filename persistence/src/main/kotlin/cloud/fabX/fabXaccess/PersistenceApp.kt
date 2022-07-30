package cloud.fabX.fabXaccess

import cloud.fabX.fabXaccess.common.LiquibaseMigrationHandler
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.Logger

class PersistenceApp(
    loggerFactory: LoggerFactory,
    private val liquibase: LiquibaseMigrationHandler
) {
    private val log: Logger = loggerFactory.invoke(this::class.java)

    fun initialise() {
        log.debug("initialising PersistenceApp...")
        val timeBefore = System.currentTimeMillis()

        liquibase.update()

        val initTime = (System.currentTimeMillis() - timeBefore).toDouble()
        log.debug("Persistence initialised in ${initTime / 1000} seconds.")
    }
}