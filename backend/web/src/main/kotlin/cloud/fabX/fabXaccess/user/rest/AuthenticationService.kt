package cloud.fabX.fabXaccess.user.rest

import arrow.core.flatMap
import arrow.core.right
import cloud.fabX.fabXaccess.common.model.SystemActor
import cloud.fabX.fabXaccess.device.model.GettingDeviceByIdentity
import cloud.fabX.fabXaccess.device.model.MacSecretIdentity
import cloud.fabX.fabXaccess.device.ws.DevicePrincipal
import cloud.fabX.fabXaccess.user.application.GettingUserByIdentity
import cloud.fabX.fabXaccess.user.model.UsernamePasswordIdentity
import io.ktor.auth.Principal
import io.ktor.auth.UserPasswordCredential

class AuthenticationService(
    private val gettingUserByIdentity: GettingUserByIdentity,
    private val gettingDeviceByIdentity: GettingDeviceByIdentity
) {
    suspend fun basic(credentials: UserPasswordCredential): Principal {
        val device = MacSecretIdentity(credentials.name, credentials.password).right()
            .flatMap {
                gettingDeviceByIdentity.getByIdentity(it)
            }

        val user = UsernamePasswordIdentity
            .fromUnvalidated(
                credentials.name,
                hash(credentials.password),
                null
            )
            .flatMap {
                gettingUserByIdentity.getUserByIdentity(
                    SystemActor,
                    it
                )
            }
            .fold(
                { ErrorPrincipal(it) },
                { UserPrincipal(it) }
            )

        return device.fold(
            { user },
            { DevicePrincipal(it) }
        )
    }
}