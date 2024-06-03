package cloud.fabX.fabXaccess.common.rest

import arrow.core.Either
import cloud.fabX.fabXaccess.common.model.Error
import io.ktor.http.CacheControl
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.cacheControl
import io.ktor.server.response.respond

internal suspend inline fun <reified T : Any> ApplicationCall.respondWithErrorHandler(
    result: Either<Error, T>,
    cacheControl: CacheControl? = null,
) {
    result
        .onRight {
            if (it == Unit) {
                respond(HttpStatusCode.NoContent)
            } else {
                cacheControl?.let { cc ->
                    this.response.cacheControl(cc)
                }
                respond(it)
            }
        }
        .onLeft { handleError(it) }
}

internal suspend fun ApplicationCall.handleError(error: Error) {
    val statusCode = when (error) {
        // authentication, authorization
        is Error.NotAuthenticated -> HttpStatusCode.Unauthorized
        is Error.InvalidAuthentication -> HttpStatusCode.Unauthorized
        is Error.UserNotInstructor -> HttpStatusCode.Forbidden
        is Error.UserNotAdmin -> HttpStatusCode.Forbidden
        is Error.UserIsLocked -> HttpStatusCode.Forbidden
        is Error.InvalidSecondFactor -> HttpStatusCode.Forbidden
        // domain shared
        is Error.ThumbnailInvalid -> HttpStatusCode.UnprocessableEntity
        // domain qualification
        is Error.QualificationNotFound -> HttpStatusCode.NotFound
        is Error.ReferencedQualificationNotFound -> HttpStatusCode.UnprocessableEntity
        is Error.QualificationInUse -> HttpStatusCode.UnprocessableEntity
        // domain tool
        is Error.ToolNotFound -> HttpStatusCode.NotFound
        is Error.ReferencedToolNotFound -> HttpStatusCode.UnprocessableEntity
        is Error.PinNotInUse -> HttpStatusCode.NotFound
        is Error.ToolTypeNotUnlock -> HttpStatusCode.UnprocessableEntity
        // domain device
        is Error.MacInvalid -> HttpStatusCode.UnprocessableEntity
        is Error.SecretInvalid -> HttpStatusCode.UnprocessableEntity
        is Error.DeviceNotFound -> HttpStatusCode.NotFound
        is Error.DeviceThumbnailNotFound -> HttpStatusCode.NotFound
        is Error.DeviceNotFoundByIdentity -> HttpStatusCode.NotFound
        is Error.PinInUse -> HttpStatusCode.UnprocessableEntity
        is Error.ToolNotAttachedToDevice -> HttpStatusCode.UnprocessableEntity
        is Error.DeviceNotActor -> HttpStatusCode.UnprocessableEntity
        // web device
        is Error.DeviceNotConnected -> HttpStatusCode.ServiceUnavailable
        is Error.DeviceTimeout -> HttpStatusCode.ServiceUnavailable
        is Error.DeviceCommunicationSerializationError -> HttpStatusCode.ServiceUnavailable
        is Error.UnexpectedDeviceResponse -> HttpStatusCode.ServiceUnavailable
        // domain user
        is Error.UserNotFound -> HttpStatusCode.NotFound
        is Error.UserNotFoundByIdentity -> HttpStatusCode.Unauthorized
        is Error.UserNotFoundByCardId -> HttpStatusCode.NotFound
        is Error.UserNotFoundByUsername -> HttpStatusCode.NotFound
        is Error.UserNotFoundByWikiName -> HttpStatusCode.NotFound
        is Error.SoftDeletedUserNotFound -> HttpStatusCode.NotFound
        is Error.WikiNameAlreadyInUse -> HttpStatusCode.UnprocessableEntity
        is Error.InstructorPermissionNotFound -> HttpStatusCode.Forbidden
        is Error.UserAlreadyAdmin -> HttpStatusCode.UnprocessableEntity
        is Error.UserAlreadyNotAdmin -> HttpStatusCode.UnprocessableEntity
        is Error.InstructorQualificationAlreadyFound -> HttpStatusCode.UnprocessableEntity
        is Error.InstructorQualificationNotFound -> HttpStatusCode.UnprocessableEntity
        is Error.MemberQualificationAlreadyFound -> HttpStatusCode.UnprocessableEntity
        is Error.MemberQualificationNotFound -> HttpStatusCode.UnprocessableEntity
        is Error.UsernameAlreadyInUse -> HttpStatusCode.UnprocessableEntity
        is Error.UsernameInvalid -> HttpStatusCode.UnprocessableEntity
        is Error.UserIdInvalid -> HttpStatusCode.UnprocessableEntity
        is Error.PasswordHashInvalid -> HttpStatusCode.UnprocessableEntity
        is Error.UsernamePasswordIdentityAlreadyFound -> HttpStatusCode.UnprocessableEntity
        is Error.UsernamePasswordIdentityNotFound -> HttpStatusCode.UnprocessableEntity
        is Error.CredentialIdAlreadyInUse -> HttpStatusCode.UnprocessableEntity
        is Error.CardIdAlreadyInUse -> HttpStatusCode.UnprocessableEntity
        is Error.CardIdInvalid -> HttpStatusCode.UnprocessableEntity
        is Error.CardSecretInvalid -> HttpStatusCode.UnprocessableEntity
        is Error.PhoneNrAlreadyInUse -> HttpStatusCode.UnprocessableEntity
        is Error.PhoneNrInvalid -> HttpStatusCode.UnprocessableEntity
        is Error.PinIdentityAlreadyFound -> HttpStatusCode.UnprocessableEntity
        is Error.PinInvalid -> HttpStatusCode.UnprocessableEntity
        is Error.UserIdentityNotFound -> HttpStatusCode.UnprocessableEntity
        is Error.UserIsActor -> HttpStatusCode.UnprocessableEntity
        is Error.UserNotActor -> HttpStatusCode.UnprocessableEntity
        // persistence
        is Error.VersionConflict -> HttpStatusCode.UnprocessableEntity
        // webauthn
        is Error.WebauthnError -> HttpStatusCode.UnprocessableEntity
        is Error.ChallengeNotFound -> HttpStatusCode.Unauthorized
    }

    if (statusCode == HttpStatusCode.Unauthorized) {
        respond(statusCode)
    } else {
        respond(statusCode, error.toRestModel())
    }
}