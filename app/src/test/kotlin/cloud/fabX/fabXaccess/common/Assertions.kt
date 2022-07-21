package cloud.fabX.fabXaccess.common

import assertk.Assert
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@ExperimentalSerializationApi
internal inline fun <reified T> Assert<String>.isJson() = transform { Json.decodeFromString<T>(it) }