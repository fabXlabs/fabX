package cloud.fabX.fabXaccess

import cloud.fabX.fabXaccess.common.SynchronousDomainEventPublisher
import cloud.fabX.fabXaccess.common.model.DomainEventPublisher
import kotlinx.datetime.Clock
import org.kodein.di.DI
import org.kodein.di.bindConstant
import org.kodein.di.bindSingleton
import org.kodein.di.instance

val app = DI {
    import(domainModule)
    import(restModule)
    import(persistenceModule)
    import(loggingModule)

    bindConstant(tag = "port") { 8080 }

    bindSingleton<DomainEventPublisher> { SynchronousDomainEventPublisher() }
    bindSingleton<Clock> { Clock.System }
}

fun main() {
    val restApp: RestApp by app.instance()
    restApp.start()
}