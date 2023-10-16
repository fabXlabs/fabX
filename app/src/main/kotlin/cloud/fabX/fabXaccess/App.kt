package cloud.fabX.fabXaccess

import cloud.fabX.fabXaccess.common.Config
import cloud.fabX.fabXaccess.common.SynchronousDomainEventPublisher
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.DomainEventHandler
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.model.SystemActorId
import cloud.fabX.fabXaccess.common.model.newCorrelationId
import cloud.fabX.fabXaccess.common.model.newUserId
import cloud.fabX.fabXaccess.user.model.IsAdminChanged
import cloud.fabX.fabXaccess.user.model.UserCreated
import cloud.fabX.fabXaccess.user.model.UserRepository
import cloud.fabX.fabXaccess.user.model.UsernamePasswordIdentityAdded
import java.io.File
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
    import(webModule)
    import(loggingModule)

    if (config.dbCaching) {
        import(cachedPersistenceModule)
    } else {
        import(persistenceModule)
    }

    bindConstant(tag = "port") { config.port }

    bindConstant(tag = "dburl") { config.dbUrl }
    bindConstant(tag = "dbuser") { config.dbUser }
    bindConstant(tag = "dbpassword") { config.dbPassword }

    bindConstant(tag = "jwtIssuer") { config.jwtIssuer }
    bindConstant(tag = "jwtAudience") { config.jwtAudience }
    bindConstant(tag = "jwtHMAC256Secret") { config.jwtHMAC256Secret }

    bindConstant(tag = "webauthnOrigin") { config.webauthnOrigin }
    bindConstant(tag = "webauthnRpId") { config.webauthnRpId }
    bindConstant(tag = "webauthnRpName") { config.webauthnRpName }

    bindConstant(tag = "deviceReceiveTimeoutMillis") { config.deviceReceiveTimeoutMillis }

    bindInstance(tag = "firmwareDirectory") { File(config.firmwareDirectory) }

    bindInstance(tag = "metricsPassword") { config.metricsPassword }

    bindInstance(tag = "httpsRedirect") { config.httpsRedirect }

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

    val webApp: WebApp by app.instance()
    webApp.start()
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
            )
        ).forEach { userRepository.store(it) }

        logger.warn("...done creating new admin account")
    }
}