package cloud.fabX.fabXaccess.common

import assertk.Assert
import assertk.all
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import cloud.fabX.fabXaccess.common.rest.Error
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@ExperimentalSerializationApi
internal inline fun <reified T> Assert<String>.isJson() = transform { Json.decodeFromString<T>(it) }

@ExperimentalSerializationApi
internal fun Assert<String?>.isError(
    type: String,
    message: String,
    parameters: Map<String, String> = mapOf()
) = isNotNull()
    .isJson<Error>()
    .all {
        transform { it.type }.isEqualTo(type)
        transform { it.message }.isEqualTo(message)
        transform { it.parameters }.isEqualTo(parameters)
    }