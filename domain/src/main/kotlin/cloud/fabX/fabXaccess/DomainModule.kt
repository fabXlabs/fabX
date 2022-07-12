package cloud.fabX.fabXaccess

import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.qualification.model.QualificationRepository
import cloud.fabX.fabXaccess.user.model.UserRepository

object DomainModule {
    private var loggerFactory: LoggerFactory? = null
    private var userRepository: UserRepository? = null
    private var qualificationRepository: QualificationRepository? = null

    fun isFullyConfigured(): Boolean {
        return loggerFactory != null
                && userRepository != null
                && qualificationRepository != null
    }

    fun configure(loggerFactory: LoggerFactory) {
        this.loggerFactory = loggerFactory
    }

    fun configure(userRepository: UserRepository) {
        this.userRepository = userRepository
    }

    fun configure(qualificationRepository: QualificationRepository) {
        this.qualificationRepository = qualificationRepository
    }

    internal fun loggerFactory(): LoggerFactory {
        return require(loggerFactory)
    }

    internal fun userRepository(): UserRepository {
        return require(userRepository)
    }

    internal fun qualificationRepository(): QualificationRepository {
        return require(qualificationRepository)
    }

    private fun <T : Any> require(value: T?): T = requireNotNull(value) { "DomainModule has to be configured" }
}