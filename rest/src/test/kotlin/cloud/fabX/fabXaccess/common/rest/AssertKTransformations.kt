package cloud.fabX.fabXaccess.common.rest

import assertk.Assert
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@ExperimentalSerializationApi
inline fun <reified T> Assert<String>.isJson() = transform { Json.decodeFromString<T>(it) }
