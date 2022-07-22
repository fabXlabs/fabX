package cloud.fabX.fabXaccess.user.rest

import arrow.core.flatMap
import cloud.fabX.fabXaccess.common.model.SystemActor
import cloud.fabX.fabXaccess.user.application.GettingUserByIdentity
import cloud.fabX.fabXaccess.user.model.UsernamePasswordIdentity
import io.ktor.auth.Principal
import io.ktor.auth.UserPasswordCredential

class AuthenticationService(
    private val gettingUserByIdentity: GettingUserByIdentity
) {
    fun basic(credentials: UserPasswordCredential): Principal {
        return UsernamePasswordIdentity
            .fromUnvalidated(
                credentials.name,
                hash(credentials.password)
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
    }
}