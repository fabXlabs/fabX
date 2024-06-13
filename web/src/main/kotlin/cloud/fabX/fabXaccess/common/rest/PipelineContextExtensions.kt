package cloud.fabX.fabXaccess.common.rest

import arrow.core.Either
import arrow.core.flatMap
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.user.model.Admin
import cloud.fabX.fabXaccess.user.model.Instructor
import cloud.fabX.fabXaccess.user.model.Member
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.util.pipeline.PipelineContext


internal suspend inline fun <reified T : Any> PipelineContext<*, ApplicationCall>.withAdminAuthRespond(
    function: (Admin) -> Either<Error, T>
) {
    return call.respondWithErrorHandler(
        readAdminAuthentication()
            .flatMap { admin ->
                function(admin)
            }
    )
}

internal suspend inline fun <reified T : Any> PipelineContext<*, ApplicationCall>.withMemberAuthRespond(
    function: (Member) -> Either<Error, T>
) {
    return call.respondWithErrorHandler(
        readMemberAuthentication()
            .flatMap { member ->
                function(member)
            }
    )
}

internal suspend inline fun <reified T : Any> PipelineContext<*, ApplicationCall>.withInstructorAuthRespond(
    function: (Instructor) -> Either<Error, T>
) {
    return call.respondWithErrorHandler(
        readInstructorAuthentication()
            .flatMap { instructor ->
                function(instructor)
            }
    )
}
