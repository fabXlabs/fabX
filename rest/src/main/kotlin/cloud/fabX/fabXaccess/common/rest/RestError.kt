package cloud.fabX.fabXaccess.common.rest

import kotlinx.serialization.Serializable

@Serializable
data class RestError(val message: String, val parameters: Map<String, String> = emptyMap())