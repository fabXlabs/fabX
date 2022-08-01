package cloud.fabX.fabXaccess

import cloud.fabX.fabXaccess.common.Config
import cloud.fabX.fabXaccess.common.SynchronousDomainEventPublisher
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.DomainEventHandler
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.model.SystemActorId
import cloud.fabX.fabXaccess.common.model.newCorrelationId
import cloud.fabX.fabXaccess.common.model.newDeviceId
import cloud.fabX.fabXaccess.common.model.newUserId
import cloud.fabX.fabXaccess.device.model.DeviceCreated
import cloud.fabX.fabXaccess.device.model.DeviceRepository
import cloud.fabX.fabXaccess.user.model.CardIdentityAdded
import cloud.fabX.fabXaccess.user.model.IsAdminChanged
import cloud.fabX.fabXaccess.user.model.UserCreated
import cloud.fabX.fabXaccess.user.model.UserRepository
import cloud.fabX.fabXaccess.user.model.UsernamePasswordIdentityAdded
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import org.kodein.di.DI
import org.kodein.di.allInstances
import org.kodein.di.bindConstant
import org.kodein.di.bindInstance
import org.kodein.di.bindSingleton
import org.kodein.di.instance

object App // logger tag

val config = Config.fromEnv()

val app = DI {
    import(domainModule)
    import(restModule)
    import(persistenceModule)
    import(loggingModule)

    bindConstant(tag = "port") { config.port }
    bindInstance(tag = "dburl") { config.dbUrl }
    bindInstance(tag = "dbuser") { config.dbUser }
    bindInstance(tag = "dbpassword") { config.dbPassword }

    bindSingleton { SynchronousDomainEventPublisher() }
    bindSingleton { Clock.System }
}

fun main() {
    val loggerFactory: LoggerFactory by app.instance()
    val logger = loggerFactory.invoke(App::class.java)

    val domainEventPublisher: SynchronousDomainEventPublisher by app.instance()
    val domainEventHandler: List<DomainEventHandler> by app.allInstances()

    domainEventHandler.forEach {
        logger.debug("adding domain event handler $it")
        domainEventPublisher.addHandler(it)
    }

    val persistenceApp: PersistenceApp by app.instance()
    persistenceApp.initialise()

    val userRepository: UserRepository by app.instance()
    createInitialAdminAccountIfUserRepoIsEmpty(userRepository, logger)

    // TODO REMOVE
    val deviceRepository: DeviceRepository by app.instance()
    runBlocking {
        if (deviceRepository.getAll().isEmpty()) {
            deviceRepository.store(
                DeviceCreated(
                    newDeviceId(),
                    SystemActorId,
                    Clock.System.now(),
                    newCorrelationId(),
                    "Demo Device",
                    "https://example.com/bg.bmp",
                    "https://example.com",
                    "AABBCCDDEEFF",
                    "abcdef0123456789abcdef0123456789"
                )
            )
        }
    }

    val restApp: RestApp by app.instance()
    restApp.start()
}

fun createInitialAdminAccountIfUserRepoIsEmpty(userRepository: UserRepository, logger: Logger) = runBlocking {
    if (userRepository.getSourcingEvents().isEmpty()) {
        logger.warn("No events are found in user repository. A new admin account with username \"admin\" and password \"password\" is created...")

        val correlationId = newCorrelationId()
        val adminUserId = newUserId()

        listOf(
            UserCreated(
                adminUserId,
                SystemActorId,
                Clock.System.now(),
                correlationId,
                firstName = "Admin",
                lastName = "",
                wikiName = "admin"
            ),
            UsernamePasswordIdentityAdded(
                adminUserId,
                2,
                SystemActorId,
                Clock.System.now(),
                correlationId,
                username = "admin",
                hash = "2o0iqAqKf1UEB2IrsWfSb3aaSL0gwyEQatwW+o6/Qf4=" // password: password
            ),
            IsAdminChanged(
                adminUserId,
                3,
                SystemActorId,
                Clock.System.now(),
                correlationId,
                isAdmin = true
            ),

            // TODO remove
            CardIdentityAdded(
                adminUserId,
                4,
                SystemActorId,
                Clock.System.now(),
                correlationId,
                "AABBCCDDEEFF11",
                "F4B726CC27C2413227382ABF095D09B1A13B00FC6AD1B1B5D75C4A954628C807"
            )

        ).forEach { userRepository.store(it) }

        logger.warn("...done creating new admin account")
    }
}