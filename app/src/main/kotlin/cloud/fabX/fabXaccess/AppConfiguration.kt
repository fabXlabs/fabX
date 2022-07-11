package cloud.fabX.fabXaccess

import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.logging.LogbackLoggerFactory
import cloud.fabX.fabXaccess.user.infrastructure.UserDatabaseRepository
import cloud.fabX.fabXaccess.user.model.UserRepository

object AppConfiguration {
    internal val loggerFactory: LoggerFactory
    private val userRepository: UserRepository

    init {
        loggerFactory = LogbackLoggerFactory()
        userRepository = UserDatabaseRepository()

        configureDomain()
    }

    private fun configureDomain() {
        DomainModule.configure(loggerFactory, userRepository)
    }

}

@Suppress("unused") // receiver T is required to infer class to create logger for
internal inline fun <reified T> T.logger(): Logger {
    return AppConfiguration.loggerFactory.invoke(T::class.java)
}