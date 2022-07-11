package cloud.fabX.fabXaccess

import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.user.model.UserRepository

object DomainModule {
    private var loggerFactory: LoggerFactory? = null
    private var userRepository: UserRepository? = null

    fun configure(loggerFactory: LoggerFactory, userRepository: UserRepository) {
        this.loggerFactory = loggerFactory
        this.userRepository = userRepository
    }

    internal fun loggerFactory(): LoggerFactory {
        return require(loggerFactory)
    }

    internal fun userRepository(): UserRepository {
        return require(userRepository)
    }

    private fun <T : Any> require(value: T?): T = requireNotNull(value) { "DomainModule has to be configured" }
}