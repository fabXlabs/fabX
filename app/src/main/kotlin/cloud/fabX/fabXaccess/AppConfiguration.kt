package cloud.fabX.fabXaccess

import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.device.infrastructure.DeviceDatabaseRepository
import cloud.fabX.fabXaccess.device.model.DeviceIdFactory
import cloud.fabX.fabXaccess.device.model.DeviceRepository
import cloud.fabX.fabXaccess.device.model.newDeviceId
import cloud.fabX.fabXaccess.logging.LogbackLoggerFactory
import cloud.fabX.fabXaccess.qualification.infrastructure.QualificationDatabaseRepository
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFactory
import cloud.fabX.fabXaccess.qualification.model.QualificationRepository
import cloud.fabX.fabXaccess.qualification.model.newQualificationId
import cloud.fabX.fabXaccess.tool.infrastructure.ToolDatabaseRepository
import cloud.fabX.fabXaccess.tool.model.GettingToolsByQualificationId
import cloud.fabX.fabXaccess.tool.model.ToolIdFactory
import cloud.fabX.fabXaccess.tool.model.ToolRepository
import cloud.fabX.fabXaccess.tool.model.newToolId
import cloud.fabX.fabXaccess.user.infrastructure.UserDatabaseRepository
import cloud.fabX.fabXaccess.user.model.GettingUserByCardId
import cloud.fabX.fabXaccess.user.model.GettingUserByIdentity
import cloud.fabX.fabXaccess.user.model.GettingUserByUsername
import cloud.fabX.fabXaccess.user.model.GettingUserByWikiName
import cloud.fabX.fabXaccess.user.model.UserIdFactory
import cloud.fabX.fabXaccess.user.model.UserRepository
import cloud.fabX.fabXaccess.user.model.newUserId
import kotlin.system.exitProcess

object AppConfiguration {

    private val log: Logger

    internal val loggerFactory: LoggerFactory
    private val deviceIdFactory: DeviceIdFactory
    private val qualificationIdFactory: QualificationIdFactory
    private val toolIdFactory: ToolIdFactory
    private val userIdFactory: UserIdFactory

    private val deviceRepository: DeviceRepository
    private val qualificationRepository: QualificationRepository
    private val toolRepository: ToolRepository
    private val userRepository: UserRepository

    private val gettingToolsByQualificationId: GettingToolsByQualificationId
    private val gettingUserByUsername: GettingUserByUsername
    private val gettingUserByIdentity: GettingUserByIdentity
    private val gettingUserByWikiName: GettingUserByWikiName
    private val gettingUserByCardId: GettingUserByCardId

    init {
        loggerFactory = LogbackLoggerFactory()
        log = loggerFactory.invoke(AppConfiguration::class.java)
        log.info("Configuring modules...")

        deviceIdFactory = { newDeviceId() }
        qualificationIdFactory = { newQualificationId() }
        toolIdFactory = { newToolId() }
        userIdFactory = { newUserId() }

        deviceRepository = DeviceDatabaseRepository()
        qualificationRepository = QualificationDatabaseRepository()
        toolRepository = ToolDatabaseRepository()
        userRepository = UserDatabaseRepository()

        gettingToolsByQualificationId = toolRepository
        gettingUserByUsername = userRepository
        gettingUserByIdentity = userRepository
        gettingUserByWikiName = userRepository
        gettingUserByCardId = userRepository

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
        DomainModule.configureLoggerFactory(loggerFactory)

        DomainModule.configureDeviceIdFactory(deviceIdFactory)
        DomainModule.configureQualificationIdFactory(qualificationIdFactory)
        DomainModule.configureToolIdFactory(toolIdFactory)
        DomainModule.configureUserIdFactory(userIdFactory)

        DomainModule.configureDeviceRepository(deviceRepository)
        DomainModule.configureQualificationRepository(qualificationRepository)
        DomainModule.configureToolRepository(toolRepository)
        DomainModule.configureUserRepository(userRepository)

        DomainModule.configureGettingToolsByQualificationId(gettingToolsByQualificationId)
        DomainModule.configureGettingUserByUsername(gettingUserByUsername)
        DomainModule.configureGettingUserByIdentity(gettingUserByIdentity)
        DomainModule.configureGettingUserByWikiName(gettingUserByWikiName)
        DomainModule.configureGettingUserByCardId(gettingUserByCardId)
    }

    private fun configureRest() {
        RestModule.configure(loggerFactory)
    }
}

@Suppress("unused") // receiver T is required to infer class to create logger for
internal inline fun <reified T> T.logger(): Logger {
    return AppConfiguration.loggerFactory.invoke(T::class.java)
}