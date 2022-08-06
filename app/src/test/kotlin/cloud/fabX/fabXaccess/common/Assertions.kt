package cloud.fabX.fabXaccess.common

import assertk.Assert
import assertk.all
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.rest.Error
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@ExperimentalSerializationApi
internal inline fun <reified T> Assert<String>.isJson() = transform { Json.decodeFromString<T>(it) }

internal fun Assert<Error>.isError(
    type: String,
    message: String,
    parameters: Map<String, String> = mapOf()
) = all {
    transform { it.type }.isEqualTo(type)
    transform { it.message }.isEqualTo(message)
    transform { it.parameters }.isEqualTo(parameters)
}