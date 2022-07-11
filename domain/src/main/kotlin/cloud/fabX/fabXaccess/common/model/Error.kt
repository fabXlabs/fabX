package cloud.fabX.fabXaccess.common.model

sealed class Error {
    data class UserNotFoundError(val message: String, val parameters: Map<String, String> = emptyMap()): Error()
}