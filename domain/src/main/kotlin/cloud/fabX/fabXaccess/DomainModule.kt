package cloud.fabX.fabXaccess

import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.device.model.DeviceIdFactory
import cloud.fabX.fabXaccess.device.model.DeviceRepository
import cloud.fabX.fabXaccess.qualification.model.QualificationRepository
import cloud.fabX.fabXaccess.user.model.UserRepository

// TODO tool aggregate

object DomainModule {
    private var loggerFactory: LoggerFactory? = null

    private var deviceIdFactory: DeviceIdFactory? = null

    private var deviceRepository: DeviceRepository? = null
    private var qualificationRepository: QualificationRepository? = null
    private var userRepository: UserRepository? = null


    fun isFullyConfigured(): Boolean {
        return loggerFactory != null
                && deviceIdFactory != null
                && deviceRepository != null
                && qualificationRepository != null
                && userRepository != null
    }

    fun configureLoggerFactory(loggerFactory: LoggerFactory) {
        this.loggerFactory = loggerFactory
    }

    fun configureDeviceIdFactory(deviceIdFactory: DeviceIdFactory) {
        this.deviceIdFactory = deviceIdFactory
    }

    fun configureDeviceRepository(deviceRepository: DeviceRepository) {
        this.deviceRepository = deviceRepository
    }

    fun configureUserRepository(userRepository: UserRepository) {
        this.userRepository = userRepository
    }

    fun configureQualificationRepository(qualificationRepository: QualificationRepository) {
        this.qualificationRepository = qualificationRepository
    }

    internal fun loggerFactory(): LoggerFactory {
        return require(loggerFactory)
    }

    internal fun deviceIdFactory(): DeviceIdFactory {
        return require(deviceIdFactory)
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