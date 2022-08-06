package cloud.fabX.fabXaccess.common

import cloud.fabX.fabXaccess.PersistenceApp
import cloud.fabX.fabXaccess.WebApp
import cloud.fabX.fabXaccess.common.model.DomainEventHandler
import cloud.fabX.fabXaccess.common.model.SystemActorId
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.common.model.newCorrelationId
import cloud.fabX.fabXaccess.device.infrastructure.DeviceSourcingEventDAO
import cloud.fabX.fabXaccess.domainModule
import cloud.fabX.fabXaccess.loggingModule
import cloud.fabX.fabXaccess.persistenceModule
import cloud.fabX.fabXaccess.qualification.infrastructure.QualificationSourcingEventDAO
import cloud.fabX.fabXaccess.tool.infrastructure.ToolSourcingEventDAO
import cloud.fabX.fabXaccess.user.infrastructure.UserSourcingEventDAO
import cloud.fabX.fabXaccess.user.model.IsAdminChanged
import cloud.fabX.fabXaccess.user.model.UserCreated
import cloud.fabX.fabXaccess.user.model.UserRepository
import cloud.fabX.fabXaccess.user.model.UsernamePasswordIdentityAdded
import cloud.fabX.fabXaccess.webModule
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.basicAuth
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.TestApplicationRequest
import io.ktor.server.testing.createTestEnvironment
import io.ktor.server.testing.testApplication
import io.ktor.server.testing.withApplication
import io.ktor.util.encodeBase64
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import org.kodein.di.allInstances
import org.kodein.di.bindConstant
import org.kodein.di.bindInstance
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

val postgresImageName = DockerImageName.parse("postgres").withTag("13")
val postgresContainer = PostgreSQLContainer(postgresImageName)

var initialised = false

@OptIn(ExperimentalCoroutinesApi::class)
private fun testSetup(): WebApp {
    if (!postgresContainer.isRunning) {
        println("starting postgres container...")
        postgresContainer.start()
        println("...started postgres container")
    }

    val testApp = DI {
        import(domainModule)
        import(webModule)
        import(persistenceModule)
        import(loggingModule)

        bindConstant(tag = "port") { -1 }
        bindConstant(tag = "deviceReceiveTimeoutMillis") { 2000L }

        bindSingleton { SynchronousDomainEventPublisher() }
        bindSingleton { Clock.System }

        bindInstance(tag = "dburl") { postgresContainer.jdbcUrl }
        bindInstance(tag = "dbuser") { postgresContainer.username }
        bindInstance(tag = "dbpassword") { postgresContainer.password }
    }

    // only initialise database once
    if (!initialised) {
        val persistenceApp: PersistenceApp by testApp.instance()
        persistenceApp.initialise()
        initialised = true
    }

    val db: Database by testApp.instance()

    transaction(db) {
        QualificationSourcingEventDAO.deleteAll()
        DeviceSourcingEventDAO.deleteAll()
        ToolSourcingEventDAO.deleteAll()
        UserSourcingEventDAO.deleteAll()
    }

    val domainEventPublisher: SynchronousDomainEventPublisher by testApp.instance()
    val domainEventHandler: List<DomainEventHandler> by testApp.allInstances()

    domainEventHandler.forEach {
        domainEventPublisher.addHandler(it)
    }

    val webApp: WebApp by testApp.instance()
    val userRepository: UserRepository by testApp.instance()

    val setupCorrelationId = newCorrelationId()
    val timestamp = Instant.fromEpochMilliseconds(1641085323000)

    val memberUserId = UserId(UUID.fromString("c63b3a7d-bd18-4272-b4ed-4bcf9683c602"))
    val memberCreated = UserCreated(
        memberUserId,
        SystemActorId,
        timestamp,
        setupCorrelationId,
        firstName = "Member",
        lastName = "",
        wikiName = "member"
    )

    val memberUsernamePasswordIdentityAdded = UsernamePasswordIdentityAdded(
        memberUserId,
        2,
        SystemActorId,
        timestamp,
        setupCorrelationId,
        username = "member",
        hash = "GTs+xQn4hIhy4gEKY0xPE6yaJVTesoxzBPk7izh0+pQ=" // password: s3cr3t
    )

    val adminUserId = UserId(UUID.fromString("337be01a-fee3-4938-8dc3-c801d37c0e95"))
    val adminCreated = UserCreated(
        adminUserId,
        SystemActorId,
        timestamp,
        setupCorrelationId,
        firstName = "Admin",
        lastName = "",
        wikiName = "admin"
    )

    val adminUsernamePasswordIdentityAdded = UsernamePasswordIdentityAdded(
        adminUserId,
        2,
        SystemActorId,
        timestamp,
        setupCorrelationId,
        username = "admin",
        hash = "G3T2Uf9olbUkPV2lFXfgqi61iyCC7i1c2qRTtV1vhNQ=" // password: super$ecr3t
    )

    val adminIsAdminChanged = IsAdminChanged(
        adminUserId,
        3,
        SystemActorId,
        timestamp,
        setupCorrelationId,
        isAdmin = true
    )

    runTest {
        listOf(
            memberCreated,
            memberUsernamePasswordIdentityAdded,
            adminCreated,
            adminUsernamePasswordIdentityAdded,
            adminIsAdminChanged
        ).forEach { userRepository.store(it) }
    }

    return webApp
}

internal fun withTestAppB(
    block: suspend ApplicationTestBuilder.() -> Unit
) {
    val webApp = testSetup()

    testApplication {
        environment {
            developmentMode = false
        }
        application {
            webApp.moduleConfiguration(this)
        }
        block()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
internal fun withTestApp(
    block: TestApplicationEngine.() -> Unit
) {
    val webApp = testSetup()

    withApplication(createTestEnvironment {
        developmentMode = false
        modules += webApp.moduleConfiguration
    }) {
        block()
    }
}

internal fun ApplicationTestBuilder.c(): HttpClient {
    return createClient {
        install(ContentNegotiation) {
            json()
        }
        install(WebSockets)
    }
}

internal fun HttpRequestBuilder.memberAuth() = basicAuth("member", "s3cr3t")

internal fun HttpRequestBuilder.adminAuth() = basicAuth("admin", "super\$ecr3t")

internal fun TestApplicationRequest.addMemberAuth() = addBasicAuth("member", "s3cr3t")

internal fun TestApplicationRequest.addAdminAuth() = addBasicAuth("admin", "super\$ecr3t")

internal fun TestApplicationRequest.addBasicAuth(user: String, password: String) {
    val encoded = "$user:$password".toByteArray(Charsets.UTF_8).encodeBase64()
    addHeader(HttpHeaders.Authorization, "Basic $encoded")
}