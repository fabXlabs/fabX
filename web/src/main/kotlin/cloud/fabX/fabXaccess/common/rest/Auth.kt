package cloud.fabX.fabXaccess.common.rest

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.device.model.DeviceActor
import cloud.fabX.fabXaccess.device.ws.DevicePrincipal
import cloud.fabX.fabXaccess.user.model.Admin
import cloud.fabX.fabXaccess.user.model.Instructor
import cloud.fabX.fabXaccess.user.model.Member
import cloud.fabX.fabXaccess.user.rest.ErrorPrincipal
import cloud.fabX.fabXaccess.user.rest.UserPrincipal
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.auth.principal
import io.ktor.server.websocket.WebSocketServerSession
import io.ktor.util.pipeline.PipelineContext

internal fun PipelineContext<*, ApplicationCall>.readAdminAuthentication(): Either<Error, Admin> {
    call.principal<UserPrincipal>()?.let { userPrincipal ->
        return userPrincipal.right()
            .flatMap { it.asAdmin() }
    }
    call.principal<ErrorPrincipal>()?.let { errorPrincipal ->
        return errorPrincipal.error.left()
    }
    return Error.NotAuthenticated("Required authentication not found.").left()
}

internal fun PipelineContext<*, ApplicationCall>.readInstructorAuthentication(): Either<Error, Instructor> {
    call.principal<UserPrincipal>()?.let { userPrincipal ->
        return userPrincipal.right()
            .flatMap { it.asInstructor() }
    }
    call.principal<ErrorPrincipal>()?.let { errorPrincipal ->
        return errorPrincipal.error.left()
    }
    return Error.NotAuthenticated("Required authentication not found.").left()
}

internal fun PipelineContext<*, ApplicationCall>.readMemberAuthentication(): Either<Error, Member> {
    call.principal<UserPrincipal>()?.let { userPrincipal ->
        return userPrincipal.right()
            .map { it.asMember() }
    }
    call.principal<ErrorPrincipal>()?.let { errorPrincipal ->
        return errorPrincipal.error.left()
    }
    return Error.NotAuthenticated("Required authentication not found.").left()
}

internal fun PipelineContext<*, ApplicationCall>.readDeviceAuthentication(): Either<Error, DeviceActor> {
    return call.readDeviceAuthentication()
}

internal fun WebSocketServerSession.readDeviceAuthentication(): Either<Error, DeviceActor> {
    return call.readDeviceAuthentication()
}

private fun ApplicationCall.readDeviceAuthentication(): Either<Error, DeviceActor> {
    this.principal<DevicePrincipal>()?.let { devicePrincipal ->
        return devicePrincipal.right()
            .map { it.device.asActor() }
    }
    this.principal<ErrorPrincipal>()?.let { errorPrincipal ->
        return errorPrincipal.error.left()
    }
    return Error.NotAuthenticated("Required authentication not found.").left()
}