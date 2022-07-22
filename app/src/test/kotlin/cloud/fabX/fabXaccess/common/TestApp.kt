package cloud.fabX.fabXaccess.common

import cloud.fabX.fabXaccess.AppConfiguration
import cloud.fabX.fabXaccess.RestModule
import cloud.fabX.fabXaccess.common.model.SystemActorId
import cloud.fabX.fabXaccess.common.model.newCorrelationId
import cloud.fabX.fabXaccess.common.model.newUserId
import cloud.fabX.fabXaccess.user.model.IsAdminChanged
import cloud.fabX.fabXaccess.user.model.UserCreated
import cloud.fabX.fabXaccess.user.model.UsernamePasswordIdentityAdded
import io.ktor.http.HttpHeaders
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.TestApplicationRequest
import io.ktor.server.testing.withTestApplication
import io.ktor.util.InternalAPI
import io.ktor.util.encodeBase64
import kotlinx.coroutines.runBlocking

internal fun withTestApp(
    block: TestApplicationEngine.() -> Unit
) {
    AppConfiguration.configure()

    val setupCorrelationId = newCorrelationId()

    val memberUserId = newUserId()
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
    ).forEach { AppConfiguration.userRepository().store(it) }

    println(AppConfiguration.userRepository().getAll())

    runBlocking {
        withTestApplication(RestModule.moduleConfiguration) {
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