package cloud.fabX.fabXaccess.common.rest

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.RoutingContext
import java.util.UUID

suspend inline fun RoutingContext.readUUIDParameter(name: String): UUID? {
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

suspend inline fun RoutingContext.readIntParameter(name: String): Int? {
    call.parameters[name]?.toIntOrNull()?.let {
        return it
    }

    call.respond(HttpStatusCode.BadRequest, "Required int parameter \"$name\" not given or invalid.")
    return null
}

suspend inline fun RoutingContext.readStringParameter(name: String): String? {
    call.parameters[name]?.let {
        return it
    }

    call.respond(HttpStatusCode.BadRequest, "Required string parameter \"$name\" not given or invalid.")
    return null
}

suspend inline fun RoutingContext.readHexStringParameter(name: String): ByteArray? {
    call.parameters[name]?.let {
        try {
            check(it.length % 2 == 0) { "Must have an even length" }

            val byteIterator = it.chunkedSequence(2)
                .map { chunk -> chunk.toInt(16).toByte() }
                .iterator()

            return ByteArray(it.length / 2) { byteIterator.next() }
        } catch (e: IllegalStateException) {
            null
        } catch (e: NumberFormatException) {
            null
        }
    }

    call.respond(HttpStatusCode.BadRequest, "Required hex string parameter \"$name\" not given or invalid.")
    return null
}
