package cloud.fabX.fabXaccess.common.rest

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.util.pipeline.PipelineContext

suspend inline fun PipelineContext<*, ApplicationCall>.requireStringQueryParameter(name: String): String? {
    call.request.queryParameters[name]?.let {
        return it
    }

    call.respond(HttpStatusCode.BadRequest, "Required query parameter \"$name\" not given or invalid.")
    return null
}