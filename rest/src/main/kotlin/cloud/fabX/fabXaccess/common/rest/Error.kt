package cloud.fabX.fabXaccess.common.rest

import kotlinx.serialization.Serializable

// TODO add correlation id
@Serializable
data class Error(
    val type: String,
    val message: String,
    val parameters: Map<String, String>,
)

fun cloud.fabX.fabXaccess.common.model.Error.toRestModel() = Error(
    type = this::class.simpleName ?: "unknown",
    message = message,
    parameters = parameters
)