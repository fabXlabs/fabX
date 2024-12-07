package cloud.fabX.fabXaccess.user.rest

import arrow.core.Either
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.rest.Principal
import cloud.fabX.fabXaccess.user.model.Admin
import cloud.fabX.fabXaccess.user.model.Instructor
import cloud.fabX.fabXaccess.user.model.Member
import cloud.fabX.fabXaccess.user.model.User

data class UserPrincipal(
    private val user: User,
    val authenticationMethod: AuthenticationMethod = AuthenticationMethod.JWT
) : Principal {
    fun asAdmin(): Either<Error, Admin> {
        return user.asAdmin()
    }

    fun asInstructor(): Either<Error, Instructor> {
        return user.asInstructor()
    }

    fun asMember(): Member {
        return user.asMember()
    }
}

enum class AuthenticationMethod {
    BASIC,
    JWT
}