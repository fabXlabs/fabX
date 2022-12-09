package cloud.fabX.fabXaccess.common.rest

import io.ktor.server.plugins.BadRequestException


private tailrec fun extract(e: Throwable): Throwable {
    val c = e.cause
    return if (c == null) {
        e
    } else {
        extract(c)
    }
}

fun extractCause(e: BadRequestException): Error {
    return e.cause?.let { extract(it) }?.let {
        Error(it::class.qualifiedName ?: "unknown", it.localizedMessage, mapOf(), null)
    } ?: Error(e::class.qualifiedName ?: "unknown", e.localizedMessage, mapOf(), null)
}