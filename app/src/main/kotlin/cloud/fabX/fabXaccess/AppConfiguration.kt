package cloud.fabX.fabXaccess

import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.logging.LogbackLoggerFactory
import cloud.fabX.fabXaccess.user.infrastructure.UserDatabaseRepository
import cloud.fabX.fabXaccess.user.model.UserRepository

object AppConfiguration {
    internal val loggerFactory: LoggerFactory
    private val userRepository: UserRepository
//    private val qualificationRepository: QualificationRepository

    init {
        loggerFactory = LogbackLoggerFactory()
        userRepository = UserDatabaseRepository()
//        qualificationRepository = TODO()

        configureDomain()
    }

    private fun configureDomain() {
        DomainModule.configure(loggerFactory)
        DomainModule.configure(userRepository)
//        DomainModule.configure(qualificationRepository)
    }

}

@Suppress("unused") // receiver T is required to infer class to create logger for
internal inline fun <reified T> T.logger(): Logger {
    return AppConfiguration.loggerFactory.invoke(T::class.java)
}