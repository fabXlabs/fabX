package cloud.fabX.fabXaccess.common.rest

import arrow.core.Either
import cloud.fabX.fabXaccess.common.model.Error
import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond

suspend inline fun <reified T : Any> ApplicationCall.respondWithErrorHandler(result: Either<Error, T>) {
    result
        .tap { respond(it) }
        .tapLeft {
            when (it) {
                is Error.QualificationNotFound -> respond(HttpStatusCode.NotFound, it.toRestModel())
                // TODO handle all cases
                else -> respond(HttpStatusCode.InternalServerError, "unmapped error")
            }
        }
}