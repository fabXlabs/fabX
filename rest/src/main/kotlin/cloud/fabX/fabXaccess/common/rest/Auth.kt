package cloud.fabX.fabXaccess.common.rest

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.user.model.Admin
import cloud.fabX.fabXaccess.user.rest.ErrorPrincipal
import cloud.fabX.fabXaccess.user.rest.UserPrincipal
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.auth.principal
import io.ktor.util.pipeline.PipelineContext

internal fun PipelineContext<*, ApplicationCall>.readAdminAuthentication(): Either<Error, Admin> {
    call.principal<UserPrincipal>()?.let { userPrincipal ->
        return userPrincipal.right()
            .flatMap { it.asAdmin() }
    }
    call.principal<ErrorPrincipal>()?.let { errorPrincipal ->
        return errorPrincipal.error.left()
    }
    return Error.NotAuthenticated("Required basic authentication not found.").left()
}