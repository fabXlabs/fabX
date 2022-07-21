package cloud.fabX.fabXaccess.common

import cloud.fabX.fabXaccess.AppConfiguration
import cloud.fabX.fabXaccess.RestModule
import cloud.fabX.fabXaccess.common.model.SystemActorId
import cloud.fabX.fabXaccess.common.model.newCorrelationId
import cloud.fabX.fabXaccess.common.model.newUserId
import cloud.fabX.fabXaccess.user.model.UserCreated
import cloud.fabX.fabXaccess.user.model.UsernamePasswordIdentityAdded
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.withTestApplication
import kotlinx.coroutines.runBlocking

internal fun withTestApp(
    block: TestApplicationEngine.() -> Unit
) {
    AppConfiguration.configure()

    val setupCorrelationId = newCorrelationId()

    val adminUserId = newUserId()
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
        hash = ""
    )

    listOf(
        adminCreated,
        adminUsernamePasswordIdentityAdded
    ).forEach { AppConfiguration.userRepository().store(it) }

    runBlocking {
        withTestApplication(RestModule.moduleConfiguration) {
            block()
        }
    }
}