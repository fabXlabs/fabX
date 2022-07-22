package cloud.fabX.fabXaccess.common.rest

import arrow.core.Either
import arrow.core.Option
import cloud.fabX.fabXaccess.common.model.Error
import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond

internal suspend inline fun <reified T : Any> ApplicationCall.respondWithErrorHandler(result: Either<Error, T>) {
    result
        .tap {
            if (it == Unit) {
                respond(HttpStatusCode.OK)
            } else {
                respond(it)
            }
        }
        .tapLeft { handleError(it) }
}

internal suspend fun ApplicationCall.respondWithErrorHandler(result: Option<Error>) {
    result
        .tapNone { respond(HttpStatusCode.OK) }
        .tap { handleError(it) }
}

internal suspend fun ApplicationCall.handleError(error: Error) {
    when (error) {
        is Error.QualificationNotFound -> respond(HttpStatusCode.NotFound, error.toRestModel())
        is Error.NotAuthenticated -> respond(HttpStatusCode.Unauthorized)
        is Error.UserNotFoundByIdentity -> respond(HttpStatusCode.Unauthorized)
        is Error.UserNotAdmin -> respond(HttpStatusCode.Forbidden, error.toRestModel())
        // TODO handle all cases
        else -> respond(HttpStatusCode.InternalServerError, "unmapped error: ${error::class.qualifiedName}")
    }
}