package cloud.fabX.fabXaccess

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
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import org.kodein.di.DI
import org.kodein.di.allInstances
import org.kodein.di.bindConstant
import org.kodein.di.bindInstance
import org.kodein.di.bindSingleton
import org.kodein.di.instance

object App // logger tag

val app = DI {
    import(domainModule)
    import(restModule)
    import(persistenceModule)
    import(loggingModule)

    // TODO make configurable (via environment variables?)
    bindConstant(tag = "port") { 8080 }

    bindInstance(tag = "dburl") { "jdbc:postgresql://localhost/postgres" }
    bindInstance(tag = "dbdriver") { "org.postgresql.Driver" }
    bindInstance(tag = "dbuser") { "postgres" }
    bindInstance(tag = "dbpassword") { "postgrespassword" }

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
            )
        ).forEach { userRepository.store(it) }

        logger.warn("...done creating new admin account")
    }
}