package cloud.fabX.fabXaccess

import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.logging.LogbackLoggerFactory
import org.kodein.di.DI
import org.kodein.di.bindSingleton

val loggingModule = DI.Module("logging") {
    bindSingleton<LoggerFactory> { LogbackLoggerFactory() }
}