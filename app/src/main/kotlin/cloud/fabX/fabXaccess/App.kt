package cloud.fabX.fabXaccess

import cloud.fabX.fabXaccess.common.SynchronousDomainEventPublisher
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.DomainEventPublisher
import cloud.fabX.fabXaccess.common.model.SystemActor
import cloud.fabX.fabXaccess.common.model.SystemActorId
import cloud.fabX.fabXaccess.common.model.newUserId
import cloud.fabX.fabXaccess.user.model.IsAdminChanged
import cloud.fabX.fabXaccess.user.model.UserCreated
import cloud.fabX.fabXaccess.user.model.UserRepository
import cloud.fabX.fabXaccess.user.model.UsernamePasswordIdentity
import cloud.fabX.fabXaccess.user.model.UsernamePasswordIdentityAdded
import java.util.UUID
import kotlinx.datetime.Clock
import org.kodein.di.DI
import org.kodein.di.bindConstant
import org.kodein.di.bindSingleton
import org.kodein.di.instance

val app = DI {
    bindConstant(tag = "port") { 8080 }

    import(domainModule)
    import(restModule)
    import(persistenceModule)
    import(loggingModule)

    bindSingleton<DomainEventPublisher> { SynchronousDomainEventPublisher() }
    bindSingleton<Clock> { Clock.System }
}

fun main() {
    val restApp: RestApp by app.instance()
    val userRepository: UserRepository by app.instance()

    val userId = newUserId()
    userRepository.store(
        UserCreated(
            aggregateRootId = userId,
            actorId = SystemActorId,
            correlationId = CorrelationId(UUID.randomUUID()),
            firstName = "some",
            lastName = "one",
            wikiName = "some.one"
        )
    )

    userRepository.store(
        UsernamePasswordIdentityAdded(
            aggregateRootId = userId,
            aggregateVersion = 2,
            actorId = SystemActorId,
            correlationId = CorrelationId(UUID.randomUUID()),
            username = "some.one",
            hash = "Fp6cwyJURizWnWI2yWSsgg3FfrFErl/+vvkgdWsBdH8=" // helloworld
        )
    )

    userRepository.store(
        IsAdminChanged(
            aggregateRootId = userId,
            aggregateVersion = 3,
            actorId = SystemActorId,
            correlationId = CorrelationId(UUID.randomUUID()),
            isAdmin = true
        )
    )

    restApp.start()
}