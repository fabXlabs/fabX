package cloud.fabX.fabXaccess.common.rest

import assertk.Assert
import assertk.all
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

inline fun <reified T> Assert<String>.isJson() = transform { Json.decodeFromString<T>(it) }

internal fun Assert<Error>.isError(
    type: String,
    message: String,
    parameters: Map<String, String> = mapOf(),
    correlationId: String? = null
) = all {
        transform { it.type }.isEqualTo(type)
        transform { it.message }.isEqualTo(message)
        transform { it.parameters }.isEqualTo(parameters)
        if (correlationId != null) {
            transform { it.correlationId }.isNotNull().isEqualTo(correlationId)
        }
    }