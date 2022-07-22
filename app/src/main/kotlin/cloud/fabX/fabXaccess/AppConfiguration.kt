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
import cloud.fabX.fabXaccess.qualification.application.AddingQualification
import cloud.fabX.fabXaccess.qualification.application.GettingQualification
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
import kotlinx.datetime.Clock

object AppConfiguration {

    private lateinit var log: Logger

    private lateinit var clock: Clock

    internal lateinit var loggerFactory: LoggerFactory
    private lateinit var deviceIdFactory: DeviceIdFactory
    private lateinit var qualificationIdFactory: QualificationIdFactory
    private lateinit var toolIdFactory: ToolIdFactory
    private lateinit var userIdFactory: UserIdFactory

    private lateinit var deviceRepository: DeviceRepository
    private lateinit var qualificationRepository: QualificationRepository
    private lateinit var toolRepository: ToolRepository
    private lateinit var userRepository: UserRepository

    private lateinit var gettingDevicesByAttachedTool: GettingDevicesByAttachedTool
    private lateinit var gettingToolsByQualificationId: GettingToolsByQualificationId
    private lateinit var gettingUserByUsername: GettingUserByUsername
    private lateinit var gettingUserByIdentity: GettingUserByIdentity
    private lateinit var gettingUserByWikiName: GettingUserByWikiName
    private lateinit var gettingUserByCardId: GettingUserByCardId
    private lateinit var gettingUsersByMemberQualification: GettingUsersByMemberQualification
    private lateinit var gettingUsersByInstructorQualification: GettingUsersByInstructorQualification

    private lateinit var domainEventPublisher: SynchronousDomainEventPublisher

    private lateinit var gettingQualification: GettingQualification
    private lateinit var addingQualification: AddingQualification
    private lateinit var gettingUserByIdentityService: cloud.fabX.fabXaccess.user.application.GettingUserByIdentity

    init {
        configure()
    }

    internal fun configure() {
        DomainModule.reset()
        RestModule.reset()

        loggerFactory = LogbackLoggerFactory()
        log = loggerFactory.invoke(AppConfiguration::class.java)
        log.info("Configuring modules...")

        clock = Clock.System

        deviceIdFactory = { newDeviceId() }
        qualificationIdFactory = { newQualificationId() }
        toolIdFactory = { newToolId() }
        userIdFactory = { newUserId() }

        val deviceDatabaseRepository = DeviceDatabaseRepository()
        val toolDatabaseRepository = ToolDatabaseRepository()
        val userDatabaseRepository = UserDatabaseRepository()
        deviceRepository = deviceDatabaseRepository
        qualificationRepository = QualificationDatabaseRepository()
        toolRepository = toolDatabaseRepository
        userRepository = userDatabaseRepository

        gettingDevicesByAttachedTool = deviceDatabaseRepository
        gettingToolsByQualificationId = toolDatabaseRepository
        gettingUserByUsername = userDatabaseRepository
        gettingUserByIdentity = userDatabaseRepository
        gettingUserByWikiName = userDatabaseRepository
        gettingUserByCardId = userDatabaseRepository
        gettingUsersByMemberQualification = userDatabaseRepository
        gettingUsersByInstructorQualification = userDatabaseRepository

        domainEventPublisher = SynchronousDomainEventPublisher()

        configureDomain()

        if (!DomainModule.isFullyConfigured()) {
            log.error("DomainModule not fully configured!")
            throw IllegalStateException("DomainModule not fully configured!")
        }

        // get domain services
        gettingQualification = DomainModule.gettingQualification()
        addingQualification = DomainModule.addingQualification()
        gettingUserByIdentityService = DomainModule.gettingUserByIdentityService()

        configureRest()

        if (!RestModule.isFullyConfigured()) {
            log.error("RestModule not fully configured!")
            throw IllegalStateException("RestModule not fully configured!")
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
        RestModule.configurePort(8080)
        RestModule.configureLoggerFactory(loggerFactory)
        RestModule.configureGettingQualification(gettingQualification)
        RestModule.configureAddingQualification(addingQualification)
        RestModule.configureGettingUserByIdentity(gettingUserByIdentityService)
    }

    internal fun userRepository(): UserRepository = userRepository
}

@Suppress("unused") // receiver T is required to infer class to create logger for
internal inline fun <reified T> T.logger(): Logger {
    return AppConfiguration.loggerFactory.invoke(T::class.java)
}