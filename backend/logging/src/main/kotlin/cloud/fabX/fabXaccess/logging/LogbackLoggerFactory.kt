package cloud.fabX.fabXaccess.logging

import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.Logger

class LogbackLoggerFactory: LoggerFactory {
    override fun invoke(clazz: Class<*>): Logger {
        return LogbackLogger(org.slf4j.LoggerFactory.getLogger(clazz))
    }
}