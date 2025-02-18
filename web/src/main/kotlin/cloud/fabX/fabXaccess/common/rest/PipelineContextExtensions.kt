package cloud.fabX.fabXaccess.common.rest

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.right
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.user.model.Admin
import cloud.fabX.fabXaccess.user.model.Instructor
import cloud.fabX.fabXaccess.user.model.Member
import io.ktor.server.routing.RoutingContext


internal suspend inline fun <reified T : Any> RoutingContext.withAdminAuthRespond(
    function: (Admin) -> Either<Error, T>
) {
    return call.respondWithErrorHandler(
        readAdminAuthentication()
            .flatMap { admin ->
                function(admin)
            }
    )
}

internal suspend inline fun <reified T : Any> RoutingContext.withMemberAuthRespond(
    function: (Member) -> Either<Error, T>
) {
    return call.respondWithErrorHandler(
        readMemberAuthentication()
            .flatMap { member ->
                function(member)
            }
    )
}

internal suspend inline fun <reified T : Any> RoutingContext.withAdminOrMemberAuthRespond(
    asAdminFunction: (Admin) -> Either<Error, T>,
    asMemberFunction: (Member) -> Either<Error, T>
) {
    call.respondWithErrorHandler(
        readAdminAuthentication()
            .flatMap { admin ->
                asAdminFunction(admin)
            }
            .swap()
            .flatMap { err ->
                if (err is Error.UserNotAdmin) {
                    readMemberAuthentication()
                        .flatMap { member ->
                            asMemberFunction(member)
                        }
                        .swap()
                } else {
                    err.right()
                }
            }
            .swap()
    )
}

internal suspend inline fun <reified T : Any> RoutingContext.withInstructorAuthRespond(
    function: (Instructor) -> Either<Error, T>
) {
    return call.respondWithErrorHandler(
        readInstructorAuthentication()
            .flatMap { instructor ->
                function(instructor)
            }
    )
}
