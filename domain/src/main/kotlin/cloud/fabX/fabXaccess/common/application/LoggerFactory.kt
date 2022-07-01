package cloud.fabX.fabXaccess.common.application

import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.model.Logger

typealias LoggerFactory = (Class<*>) -> Logger

internal inline fun <reified T> T.logger(): Logger {
    return DomainModule.loggerFactory().invoke(T::class.java)
}