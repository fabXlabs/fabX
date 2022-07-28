package cloud.fabX.fabXaccess.common.rest

import arrow.core.Either
import cloud.fabX.fabXaccess.common.model.Error
import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond

internal suspend inline fun <reified T : Any> ApplicationCall.respondWithErrorHandler(result: Either<Error, T>) {
    result
        .tap {
            if (it == Unit) {
                // TODO Change to NoContent
                respond(HttpStatusCode.OK)
            } else {
                respond(it)
            }
        }
        .tapLeft { handleError(it) }
}

internal suspend fun ApplicationCall.handleError(error: Error) {
    when (error) {
        // authentication, authorization
        is Error.NotAuthenticated -> respond(HttpStatusCode.Unauthorized)
        is Error.UserNotInstructor -> respond(HttpStatusCode.Forbidden, error.toRestModel())
        is Error.UserNotAdmin -> respond(HttpStatusCode.Forbidden, error.toRestModel())
        // domain qualification
        is Error.QualificationNotFound -> respond(HttpStatusCode.NotFound, error.toRestModel())
        is Error.ReferencedQualificationNotFound -> respond(HttpStatusCode.UnprocessableEntity, error.toRestModel())
        // domain tool
        is Error.ToolNotFound -> respond(HttpStatusCode.NotFound, error.toRestModel())
        is Error.ReferencedToolNotFound -> respond(HttpStatusCode.UnprocessableEntity, error.toRestModel())
        is Error.PinNotInUse -> respond(HttpStatusCode.NotFound, error.toRestModel())
        // domain device
        is Error.DeviceNotFound -> respond(HttpStatusCode.NotFound, error.toRestModel())
        // domain user
        is Error.UserNotFound -> respond(HttpStatusCode.NotFound, error.toRestModel())
        is Error.UserNotFoundByIdentity -> respond(HttpStatusCode.Unauthorized)
        is Error.WikiNameAlreadyInUse -> respond(HttpStatusCode.UnprocessableEntity, error.toRestModel())
        is Error.UserAlreadyAdmin -> respond(HttpStatusCode.UnprocessableEntity, error.toRestModel())
        is Error.UserAlreadyNotAdmin -> respond(HttpStatusCode.UnprocessableEntity, error.toRestModel())
        is Error.InstructorQualificationAlreadyFound -> respond(HttpStatusCode.UnprocessableEntity, error.toRestModel())
        is Error.InstructorQualificationNotFound -> respond(HttpStatusCode.UnprocessableEntity, error.toRestModel())
        is Error.MemberQualificationAlreadyFound -> respond(HttpStatusCode.UnprocessableEntity, error.toRestModel())
        is Error.MemberQualificationNotFound -> respond(HttpStatusCode.UnprocessableEntity, error.toRestModel())
        is Error.UsernamePasswordIdentityAlreadyFound -> respond(
            HttpStatusCode.UnprocessableEntity,
            error.toRestModel()
        )
        is Error.UserIdentityNotFound -> respond(HttpStatusCode.UnprocessableEntity, error.toRestModel())
        // persistence
        is Error.VersionConflict -> respond(HttpStatusCode.UnprocessableEntity, error.toRestModel())

        // TODO handle all cases
        else -> respond(HttpStatusCode.InternalServerError, "unmapped error: ${error::class.qualifiedName}")
    }
}