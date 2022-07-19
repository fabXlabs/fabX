package cloud.fabX.fabXaccess

import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.device.model.DeviceIdFactory
import cloud.fabX.fabXaccess.device.model.DeviceRepository
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFactory
import cloud.fabX.fabXaccess.qualification.model.QualificationRepository
import cloud.fabX.fabXaccess.tool.model.GettingToolsByQualificationId
import cloud.fabX.fabXaccess.tool.model.ToolIdFactory
import cloud.fabX.fabXaccess.tool.model.ToolRepository
import cloud.fabX.fabXaccess.user.model.GettingUserByCardId
import cloud.fabX.fabXaccess.user.model.GettingUserByIdentity
import cloud.fabX.fabXaccess.user.model.GettingUserByUsername
import cloud.fabX.fabXaccess.user.model.GettingUserByWikiName
import cloud.fabX.fabXaccess.user.model.UserIdFactory
import cloud.fabX.fabXaccess.user.model.UserRepository

object DomainModule {
    private var loggerFactory: LoggerFactory? = null

    private var deviceIdFactory: DeviceIdFactory? = null
    private var qualificationIdFactory: QualificationIdFactory? = null
    private var toolIdFactory: ToolIdFactory? = null
    private var userIdFactory: UserIdFactory? = null

    private var deviceRepository: DeviceRepository? = null
    private var qualificationRepository: QualificationRepository? = null
    private var toolRepository: ToolRepository? = null
    private var userRepository: UserRepository? = null

    private var gettingToolsByQualificationId: GettingToolsByQualificationId? = null
    private var gettingUserByUsername: GettingUserByUsername? = null
    private var gettingUserByIdentity: GettingUserByIdentity? = null
    private var gettingUserByWikiName: GettingUserByWikiName? = null
    private var gettingUserByCardId: GettingUserByCardId? = null


    fun isFullyConfigured(): Boolean {
        return try {
            require(loggerFactory)
            require(deviceIdFactory)
            require(qualificationIdFactory)
            require(toolIdFactory)
            require(userIdFactory)
            require(deviceRepository)
            require(qualificationRepository)
            require(toolRepository)
            require(userRepository)
            require(gettingToolsByQualificationId)
            require(gettingUserByUsername)
            require(gettingUserByIdentity)
            require(gettingUserByWikiName)
            require(gettingUserByCardId)

            true
        } catch (e: IllegalArgumentException) {
            System.err.println(e.message)
            false
        }
    }

    fun configureLoggerFactory(loggerFactory: LoggerFactory) {
        this.loggerFactory = loggerFactory
    }

    fun configureDeviceIdFactory(deviceIdFactory: DeviceIdFactory) {
        this.deviceIdFactory = deviceIdFactory
    }

    fun configureQualificationIdFactory(qualificationIdFactory: QualificationIdFactory) {
        this.qualificationIdFactory = qualificationIdFactory
    }

    fun configureToolIdFactory(toolIdFactory: ToolIdFactory) {
        this.toolIdFactory = toolIdFactory
    }

    fun configureUserIdFactory(userIdFactory: UserIdFactory) {
        this.userIdFactory = userIdFactory
    }

    fun configureDeviceRepository(deviceRepository: DeviceRepository) {
        this.deviceRepository = deviceRepository
    }

    fun configureQualificationRepository(qualificationRepository: QualificationRepository) {
        this.qualificationRepository = qualificationRepository
    }

    fun configureToolRepository(toolRepository: ToolRepository) {
        this.toolRepository = toolRepository
    }

    fun configureUserRepository(userRepository: UserRepository) {
        this.userRepository = userRepository
    }

    fun configureGettingToolsByQualificationId(gettingToolsByQualificationId: GettingToolsByQualificationId) {
        this.gettingToolsByQualificationId = gettingToolsByQualificationId
    }

    fun configureGettingUserByUsername(gettingUserByUsername: GettingUserByUsername) {
        this.gettingUserByUsername = gettingUserByUsername
    }

    fun configureGettingUserByIdentity(gettingUserByIdentity: GettingUserByIdentity) {
        this.gettingUserByIdentity = gettingUserByIdentity
    }

    fun configureGettingUserByWikiName(gettingUserByWikiName: GettingUserByWikiName) {
        this.gettingUserByWikiName = gettingUserByWikiName
    }

    fun configureGettingUserByCardId(gettingUserByCardId: GettingUserByCardId) {
        this.gettingUserByCardId = gettingUserByCardId
    }

    internal fun loggerFactory(): LoggerFactory {
        return require(loggerFactory)
    }

    internal fun deviceIdFactory(): DeviceIdFactory {
        return require(deviceIdFactory)
    }

    internal fun qualificationIdFactory(): QualificationIdFactory {
        return require(qualificationIdFactory)
    }

    internal fun toolIdFactory(): ToolIdFactory {
        return require(toolIdFactory)
    }

    internal fun userIdFactory(): UserIdFactory {
        return require(userIdFactory)
    }

    internal fun deviceRepository(): DeviceRepository {
        return require(deviceRepository)
    }

    internal fun qualificationRepository(): QualificationRepository {
        return require(qualificationRepository)
    }

    internal fun toolRepository(): ToolRepository {
        return require(toolRepository)
    }

    internal fun userRepository(): UserRepository {
        return require(userRepository)
    }

    internal fun gettingToolsQualificationId(): GettingToolsByQualificationId {
        return require(gettingToolsByQualificationId)
    }

    internal fun gettingUserByUsername(): GettingUserByUsername {
        return require(gettingUserByUsername)
    }

    internal fun gettingUserByIdentity(): GettingUserByIdentity {
        return require(gettingUserByIdentity)
    }

    internal fun gettingUserByWikiName(): GettingUserByWikiName {
        return require(gettingUserByWikiName)
    }

    internal fun gettingUserByCardId(): GettingUserByCardId {
        return require(gettingUserByCardId)
    }

    private inline fun <reified T : Any> require(value: T?): T =
        requireNotNull(value) { "DomainModule has to be configured (missing ${T::class.qualifiedName})" }
}