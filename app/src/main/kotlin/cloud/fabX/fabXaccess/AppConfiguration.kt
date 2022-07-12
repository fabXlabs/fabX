package cloud.fabX.fabXaccess

import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.logging.LogbackLoggerFactory
import cloud.fabX.fabXaccess.user.infrastructure.UserDatabaseRepository
import cloud.fabX.fabXaccess.user.model.UserRepository
import kotlin.system.exitProcess

object AppConfiguration {

    private val log: Logger

    internal val loggerFactory: LoggerFactory
    private val userRepository: UserRepository
//    private val qualificationRepository: QualificationRepository

    init {
        loggerFactory = LogbackLoggerFactory()
        log = loggerFactory.invoke(AppConfiguration::class.java)
        log.info("Configuring modules...")

        userRepository = UserDatabaseRepository()
//        qualificationRepository = TODO()

        configureDomain()
        configureRest()

        if (!DomainModule.isFullyConfigured()) {
            log.error("DomainModule not fully configured!")
            exitProcess(-1)
        }

        if (!RestModule.isFullyConfigured()) {
            log.error("RestModule not fully configured!")
            exitProcess(-1)
        }

        log.info("...all modules configured")
    }

    private fun configureDomain() {
        DomainModule.configure(loggerFactory)
        DomainModule.configure(userRepository)
//        DomainModule.configure(qualificationRepository)
    }

    private fun configureRest() {
        RestModule.configure(loggerFactory)
    }
}

@Suppress("unused") // receiver T is required to infer class to create logger for
internal inline fun <reified T> T.logger(): Logger {
    return AppConfiguration.loggerFactory.invoke(T::class.java)
}