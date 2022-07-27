package cloud.fabX.fabXaccess

import cloud.fabX.fabXaccess.common.SynchronousDomainEventPublisher
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.DomainEventHandler
import kotlinx.datetime.Clock
import org.kodein.di.DI
import org.kodein.di.allInstances
import org.kodein.di.bindConstant
import org.kodein.di.bindSingleton
import org.kodein.di.instance

object App // logger tag

val app = DI {
    import(domainModule)
    import(restModule)
    import(persistenceModule)
    import(loggingModule)

    bindConstant(tag = "port") { 8080 }

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

    val restApp: RestApp by app.instance()
    restApp.start()
}