package cloud.fabX.fabXaccess

import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.logging.LogbackLoggerFactory

object AppConfiguration {
    val loggerFactory: LoggerFactory

    init {
        loggerFactory = LogbackLoggerFactory()

        configureDomain()
    }

    private fun configureDomain() {
        DomainModule.configure(loggerFactory)
    }

}

internal inline fun <reified T> T.logger(): Logger {
    return AppConfiguration.loggerFactory.invoke(T::class.java)
}