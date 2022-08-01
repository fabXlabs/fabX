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
                respond(HttpStatusCode.NoContent)
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
        is Error.QualificationInUse -> respond(HttpStatusCode.UnprocessableEntity, error.toRestModel())
        // domain tool
        is Error.ToolNotFound -> respond(HttpStatusCode.NotFound, error.toRestModel())
        is Error.ReferencedToolNotFound -> respond(HttpStatusCode.UnprocessableEntity, error.toRestModel())
        is Error.PinNotInUse -> respond(HttpStatusCode.NotFound, error.toRestModel())
        // domain device
        is Error.DeviceNotFound -> respond(HttpStatusCode.NotFound, error.toRestModel())
        is Error.DeviceNotFoundByIdentity -> respond(HttpStatusCode.NotFound, error.toRestModel())
        is Error.PinInUse -> respond(HttpStatusCode.UnprocessableEntity, error.toRestModel())
        // domain user
        is Error.UserNotFound -> respond(HttpStatusCode.NotFound, error.toRestModel())
        is Error.UserNotFoundByIdentity -> respond(HttpStatusCode.Unauthorized)
        is Error.UserNotFoundByCardId -> respond(HttpStatusCode.NotFound, error.toRestModel())
        is Error.UserNotFoundByUsername -> respond(HttpStatusCode.NotFound, error.toRestModel())
        is Error.UserNotFoundByWikiName -> respond(HttpStatusCode.NotFound, error.toRestModel())
        is Error.WikiNameAlreadyInUse -> respond(HttpStatusCode.UnprocessableEntity, error.toRestModel())
        is Error.InstructorPermissionNotFound -> respond(HttpStatusCode.Forbidden, error.toRestModel())
        is Error.UserAlreadyAdmin -> respond(HttpStatusCode.UnprocessableEntity, error.toRestModel())
        is Error.UserAlreadyNotAdmin -> respond(HttpStatusCode.UnprocessableEntity, error.toRestModel())
        is Error.InstructorQualificationAlreadyFound -> respond(HttpStatusCode.UnprocessableEntity, error.toRestModel())
        is Error.InstructorQualificationNotFound -> respond(HttpStatusCode.UnprocessableEntity, error.toRestModel())
        is Error.MemberQualificationAlreadyFound -> respond(HttpStatusCode.UnprocessableEntity, error.toRestModel())
        is Error.MemberQualificationNotFound -> respond(HttpStatusCode.UnprocessableEntity, error.toRestModel())
        is Error.UsernameAlreadyInUse -> respond(HttpStatusCode.UnprocessableEntity, error.toRestModel())
        is Error.UsernameInvalid -> respond(HttpStatusCode.UnprocessableEntity, error.toRestModel())
        is Error.PasswordHashInvalid -> respond(HttpStatusCode.UnprocessableEntity, error.toRestModel())
        is Error.UsernamePasswordIdentityAlreadyFound -> respond(
            HttpStatusCode.UnprocessableEntity,
            error.toRestModel()
        )
        is Error.CardIdAlreadyInUse -> respond(HttpStatusCode.UnprocessableEntity, error.toRestModel())
        is Error.CardIdInvalid -> respond(HttpStatusCode.UnprocessableEntity, error.toRestModel())
        is Error.CardSecretInvalid -> respond(HttpStatusCode.UnprocessableEntity, error.toRestModel())
        is Error.PhoneNrAlreadyInUse -> respond(HttpStatusCode.UnprocessableEntity, error.toRestModel())
        is Error.PhoneNrInvalid -> respond(HttpStatusCode.UnprocessableEntity, error.toRestModel())
        is Error.UserIdentityNotFound -> respond(HttpStatusCode.UnprocessableEntity, error.toRestModel())
        // persistence
        is Error.VersionConflict -> respond(HttpStatusCode.UnprocessableEntity, error.toRestModel())
    }
}