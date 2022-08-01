package cloud.fabX.fabXaccess.common.rest

import kotlinx.serialization.Serializable

@Serializable
data class Error(
    val type: String,
    val message: String,
    val parameters: Map<String, String>,
    val correlationId: String?
)

fun cloud.fabX.fabXaccess.common.model.Error.toRestModel() = Error(
    type = this::class.simpleName ?: "unknown",
    message = message,
    parameters = parameters,
    correlationId = correlationId?.serialize()
)