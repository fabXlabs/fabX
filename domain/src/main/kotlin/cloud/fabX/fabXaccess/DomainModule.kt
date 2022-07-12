package cloud.fabX.fabXaccess

import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.device.model.DeviceRepository
import cloud.fabX.fabXaccess.qualification.model.QualificationRepository
import cloud.fabX.fabXaccess.user.model.UserRepository

object DomainModule {
    private var loggerFactory: LoggerFactory? = null

    private var deviceRepository: DeviceRepository? = null
    private var qualificationRepository: QualificationRepository? = null
    private var userRepository: UserRepository? = null


    fun isFullyConfigured(): Boolean {
        return loggerFactory != null
                && deviceRepository != null
                && qualificationRepository != null
                && userRepository != null
    }

    fun configure(loggerFactory: LoggerFactory) {
        this.loggerFactory = loggerFactory
    }

    fun configure(deviceRepository: DeviceRepository) {
        this.deviceRepository = deviceRepository
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

    internal fun deviceRepository(): DeviceRepository {
        return require(deviceRepository)
    }

    internal fun qualificationRepository(): QualificationRepository {
        return require(qualificationRepository)
    }

    internal fun userRepository(): UserRepository {
        return require(userRepository)
    }

    private fun <T : Any> require(value: T?): T = requireNotNull(value) { "DomainModule has to be configured" }
}