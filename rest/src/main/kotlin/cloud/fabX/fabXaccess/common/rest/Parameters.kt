package cloud.fabX.fabXaccess.common.rest

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.util.pipeline.PipelineContext
import java.util.UUID

suspend inline fun PipelineContext<*, ApplicationCall>.requireUUIDParameter(name: String, orElse: () -> Unit): UUID {
    call.parameters[name]?.let { UUID.fromString(it) }?.let {
        return it
    }

    call.respond(HttpStatusCode.BadRequest, "Required UUID parameter \"$name\" not given.")
    orElse()

    throw IllegalArgumentException("Required UUID parameter \"$name\" not given.")
}

