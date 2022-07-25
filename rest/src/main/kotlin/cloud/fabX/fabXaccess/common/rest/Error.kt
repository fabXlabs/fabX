package cloud.fabX.fabXaccess.common.rest

import kotlinx.serialization.Serializable

@Serializable
data class Error(val message: String, val parameters: Map<String, String>)

fun cloud.fabX.fabXaccess.common.model.Error.toRestModel() = Error(
    message = message,
    parameters = parameters
)