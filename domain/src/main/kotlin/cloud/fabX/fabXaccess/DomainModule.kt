package cloud.fabX.fabXaccess

import cloud.fabX.fabXaccess.common.application.LoggerFactory

object DomainModule {
    private var loggerFactory: LoggerFactory? = null

    fun configure(loggerFactory: LoggerFactory) {
        this.loggerFactory = loggerFactory
    }

    internal fun loggerFactory(): LoggerFactory {
        val factory = loggerFactory
        requireNotNull(factory) { "DomainModule has to be configured" }
        return factory
    }
}