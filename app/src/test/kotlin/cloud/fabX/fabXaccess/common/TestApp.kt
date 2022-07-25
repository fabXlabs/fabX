package cloud.fabX.fabXaccess.common

import cloud.fabX.fabXaccess.RestApp
import cloud.fabX.fabXaccess.common.model.DomainEventPublisher
import cloud.fabX.fabXaccess.common.model.SystemActorId
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.common.model.newCorrelationId
import cloud.fabX.fabXaccess.domainModule
import cloud.fabX.fabXaccess.loggingModule
import cloud.fabX.fabXaccess.persistenceModule
import cloud.fabX.fabXaccess.restModule
import cloud.fabX.fabXaccess.user.model.IsAdminChanged
import cloud.fabX.fabXaccess.user.model.UserCreated
import cloud.fabX.fabXaccess.user.model.UserRepository
import cloud.fabX.fabXaccess.user.model.UsernamePasswordIdentityAdded
import io.ktor.http.HttpHeaders
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.TestApplicationRequest
import io.ktor.server.testing.withTestApplication
import io.ktor.util.InternalAPI
import io.ktor.util.encodeBase64
import java.util.UUID
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import org.kodein.di.DI
import org.kodein.di.bindConstant
import org.kodein.di.bindSingleton
import org.kodein.di.instance

internal fun withTestApp(
    block: TestApplicationEngine.() -> Unit
) {
    val testApp = DI {
        import(domainModule)
        import(restModule)
        import(persistenceModule)
        import(loggingModule)

        bindConstant(tag = "port") { -1 }

        bindSingleton<DomainEventPublisher> { SynchronousDomainEventPublisher() }
        bindSingleton<Clock> { Clock.System }
    }

    val restApp: RestApp by testApp.instance()
    val userRepository: UserRepository by testApp.instance()

    val setupCorrelationId = newCorrelationId()

    val memberUserId = UserId(UUID.fromString("c63b3a7d-bd18-4272-b4ed-4bcf9683c602"))
    val memberCreated = UserCreated(
        memberUserId,
        SystemActorId,
        setupCorrelationId,
        firstName = "Member",
        lastName = "",
        wikiName = "member"
    )

    val memberUsernamePasswordIdentityAdded = UsernamePasswordIdentityAdded(
        memberUserId,
        2,
        SystemActorId,
        setupCorrelationId,
        username = "member",
        hash = "GTs+xQn4hIhy4gEKY0xPE6yaJVTesoxzBPk7izh0+pQ=" // password: s3cr3t
    )

    val adminUserId = UserId(UUID.fromString("337be01a-fee3-4938-8dc3-c801d37c0e95"))
    val adminCreated = UserCreated(
        adminUserId,
        SystemActorId,
        setupCorrelationId,
        firstName = "Admin",
        lastName = "",
        wikiName = "admin"
    )

    val adminUsernamePasswordIdentityAdded = UsernamePasswordIdentityAdded(
        adminUserId,
        2,
        SystemActorId,
        setupCorrelationId,
        username = "admin",
        hash = "G3T2Uf9olbUkPV2lFXfgqi61iyCC7i1c2qRTtV1vhNQ=" // password: super$ecr3t
    )

    val adminIsAdminChanged = IsAdminChanged(
        adminUserId,
        3,
        SystemActorId,
        setupCorrelationId,
        isAdmin = true
    )

    listOf(
        memberCreated,
        memberUsernamePasswordIdentityAdded,
        adminCreated,
        adminUsernamePasswordIdentityAdded,
        adminIsAdminChanged
    ).forEach { userRepository.store(it) }

    runBlocking {
        withTestApplication(restApp.moduleConfiguration) {
            block()
        }
    }
}

@InternalAPI
internal fun TestApplicationRequest.addMemberAuth() = addBasicAuth("member", "s3cr3t")

@InternalAPI
internal fun TestApplicationRequest.addAdminAuth() = addBasicAuth("admin", "super\$ecr3t")

@InternalAPI
internal fun TestApplicationRequest.addBasicAuth(user: String, password: String) {
    val encoded = "$user:$password".toByteArray(Charsets.UTF_8).encodeBase64()
    addHeader(HttpHeaders.Authorization, "Basic $encoded")
}