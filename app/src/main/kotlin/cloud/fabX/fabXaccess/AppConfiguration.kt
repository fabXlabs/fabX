package cloud.fabX.fabXaccess

import cloud.fabX.fabXaccess.common.SynchronousDomainEventPublisher
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.DeviceIdFactory
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.model.QualificationIdFactory
import cloud.fabX.fabXaccess.common.model.ToolIdFactory
import cloud.fabX.fabXaccess.common.model.UserIdFactory
import cloud.fabX.fabXaccess.common.model.newDeviceId
import cloud.fabX.fabXaccess.common.model.newQualificationId
import cloud.fabX.fabXaccess.common.model.newToolId
import cloud.fabX.fabXaccess.common.model.newUserId
import cloud.fabX.fabXaccess.device.infrastructure.DeviceDatabaseRepository
import cloud.fabX.fabXaccess.device.model.DeviceRepository
import cloud.fabX.fabXaccess.device.model.GettingDevicesByAttachedTool
import cloud.fabX.fabXaccess.logging.LogbackLoggerFactory
import cloud.fabX.fabXaccess.qualification.infrastructure.QualificationDatabaseRepository
import cloud.fabX.fabXaccess.qualification.model.QualificationRepository
import cloud.fabX.fabXaccess.tool.infrastructure.ToolDatabaseRepository
import cloud.fabX.fabXaccess.tool.model.GettingToolsByQualificationId
import cloud.fabX.fabXaccess.tool.model.ToolRepository
import cloud.fabX.fabXaccess.user.infrastructure.UserDatabaseRepository
import cloud.fabX.fabXaccess.user.model.GettingUserByCardId
import cloud.fabX.fabXaccess.user.model.GettingUserByIdentity
import cloud.fabX.fabXaccess.user.model.GettingUserByUsername
import cloud.fabX.fabXaccess.user.model.GettingUserByWikiName
import cloud.fabX.fabXaccess.user.model.GettingUsersByInstructorQualification
import cloud.fabX.fabXaccess.user.model.GettingUsersByMemberQualification
import cloud.fabX.fabXaccess.user.model.UserRepository
import kotlin.system.exitProcess
import kotlinx.datetime.Clock

object AppConfiguration {

    private val log: Logger

    private val clock: Clock

    internal val loggerFactory: LoggerFactory
    private val deviceIdFactory: DeviceIdFactory
    private val qualificationIdFactory: QualificationIdFactory
    private val toolIdFactory: ToolIdFactory
    private val userIdFactory: UserIdFactory

    private val deviceRepository: DeviceRepository
    private val qualificationRepository: QualificationRepository
    private val toolRepository: ToolRepository
    private val userRepository: UserRepository

    private val gettingDevicesByAttachedTool: GettingDevicesByAttachedTool
    private val gettingToolsByQualificationId: GettingToolsByQualificationId
    private val gettingUserByUsername: GettingUserByUsername
    private val gettingUserByIdentity: GettingUserByIdentity
    private val gettingUserByWikiName: GettingUserByWikiName
    private val gettingUserByCardId: GettingUserByCardId
    private val gettingUsersByMemberQualification: GettingUsersByMemberQualification
    private val gettingUsersByInstructorQualification: GettingUsersByInstructorQualification

    private val domainEventPublisher: SynchronousDomainEventPublisher

    init {
        loggerFactory = LogbackLoggerFactory()
        log = loggerFactory.invoke(AppConfiguration::class.java)
        log.info("Configuring modules...")

        clock = Clock.System

        deviceIdFactory = { newDeviceId() }
        qualificationIdFactory = { newQualificationId() }
        toolIdFactory = { newToolId() }
        userIdFactory = { newUserId() }

        deviceRepository = DeviceDatabaseRepository()
        qualificationRepository = QualificationDatabaseRepository()
        toolRepository = ToolDatabaseRepository()
        userRepository = UserDatabaseRepository()

        gettingDevicesByAttachedTool = deviceRepository
        gettingToolsByQualificationId = toolRepository
        gettingUserByUsername = userRepository
        gettingUserByIdentity = userRepository
        gettingUserByWikiName = userRepository
        gettingUserByCardId = userRepository
        gettingUsersByMemberQualification = userRepository
        gettingUsersByInstructorQualification = userRepository

        domainEventPublisher = SynchronousDomainEventPublisher()

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

        DomainModule.configureClock(clock)

        DomainModule.configureDeviceIdFactory(deviceIdFactory)
        DomainModule.configureQualificationIdFactory(qualificationIdFactory)
        DomainModule.configureToolIdFactory(toolIdFactory)
        DomainModule.configureUserIdFactory(userIdFactory)

        DomainModule.configureDeviceRepository(deviceRepository)
        DomainModule.configureQualificationRepository(qualificationRepository)
        DomainModule.configureToolRepository(toolRepository)
        DomainModule.configureUserRepository(userRepository)

        DomainModule.configureGettingDevicesByTool(gettingDevicesByAttachedTool)
        DomainModule.configureGettingToolsByQualificationId(gettingToolsByQualificationId)
        DomainModule.configureGettingUserByUsername(gettingUserByUsername)
        DomainModule.configureGettingUserByIdentity(gettingUserByIdentity)
        DomainModule.configureGettingUserByWikiName(gettingUserByWikiName)
        DomainModule.configureGettingUserByCardId(gettingUserByCardId)
        DomainModule.configureGettingUsersByMemberQualification(gettingUsersByMemberQualification)
        DomainModule.configureGettingUsersByInstructorQualification(gettingUsersByInstructorQualification)

        DomainModule.configureDomainEventPublisher(domainEventPublisher)
        domainEventPublisher.addHandler(DomainModule.userDomainEventHandler())
        domainEventPublisher.addHandler(DomainModule.deviceDomainEventHandler())
    }

    private fun configureRest() {
        RestModule.configure(loggerFactory)
    }
}

@Suppress("unused") // receiver T is required to infer class to create logger for
internal inline fun <reified T> T.logger(): Logger {
    return AppConfiguration.loggerFactory.invoke(T::class.java)
}