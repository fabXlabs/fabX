package cloud.fabX.fabXaccess.common.rest

import cloud.fabX.fabXaccess.logger
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.ContentTransformationException
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.util.pipeline.PipelineContext

internal suspend inline fun <reified T : Any> PipelineContext<*, ApplicationCall>.readBody(): T? {
    return try {
        call.receive()
    } catch (e: ContentTransformationException) {
        logger().debug(e.stackTraceToString())
        call.respond(
            HttpStatusCode.BadRequest,
            "Cannot deserialize request body as type ${T::class.qualifiedName}: ${e.localizedMessage}"
        )
        null
    }
}