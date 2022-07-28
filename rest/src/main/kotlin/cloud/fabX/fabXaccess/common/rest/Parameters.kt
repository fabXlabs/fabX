package cloud.fabX.fabXaccess.common.rest

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.util.pipeline.PipelineContext
import java.util.UUID

suspend inline fun PipelineContext<*, ApplicationCall>.readUUIDParameter(name: String): UUID? {
    call.parameters[name]?.let {
        try {
            UUID.fromString(it)
        } catch (e: IllegalArgumentException) {
            null
        }
    }?.let {
        return it
    }

    call.respond(HttpStatusCode.BadRequest, "Required UUID parameter \"$name\" not given or invalid.")
    return null
}

suspend inline fun PipelineContext<*, ApplicationCall>.readIntParameter(name: String): Int? {
    call.parameters[name]?.toIntOrNull()?.let {
        return it
    }

    call.respond(HttpStatusCode.BadRequest, "Required int parameter \"$name\" not given or invalid.")
    return null
}

suspend inline fun PipelineContext<*, ApplicationCall>.readStringParameter(name: String): String? {
    call.parameters[name]?.let {
        return it
    }

    call.respond(HttpStatusCode.BadRequest, "Required string parameter \"$name\" not given or invalid.")
    return null
}