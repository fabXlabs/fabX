package cloud.fabX.fabXaccess.common.model

sealed class Error {
    data class UserNotFound(val message: String, val parameters: Map<String, String> = emptyMap()): Error()
    data class UserNotInstructor(val message: String): Error()
    data class UserNotAdmin(val message: String): Error()
    data class VersionConflict(val message: String): Error()
}