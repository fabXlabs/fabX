package cloud.fabX.fabXaccess.common.rest

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.ContentTransformationException
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.util.pipeline.PipelineContext

internal suspend inline fun <reified T : Any> PipelineContext<*, ApplicationCall>.readBody(): T? {
    return try {
        call.receive()
    } catch (e: ContentTransformationException) {
        call.respond(
            HttpStatusCode.BadRequest,
            "Cannot deserialize request body as type ${T::class.qualifiedName}: ${e.localizedMessage}"
        )
        null
    }
}