package cloud.fabX.fabXaccess.common.model

import cloud.fabX.fabXaccess.device.model.DeviceId
import cloud.fabX.fabXaccess.qualification.model.QualificationId
import cloud.fabX.fabXaccess.user.model.UserId

sealed class Error(open val message: String, val parameters: Map<String, String> = emptyMap()) {
    data class UserNotFound(
        override val message: String,
        val userId: UserId
    ) : Error(message, mapOf("userId" to userId.toString()))

    data class UserNotInstructor(override val message: String) : Error(message)
    data class UserNotAdmin(override val message: String) : Error(message)

    data class QualificationNotFound(
        override val message: String,
        val qualificationId: QualificationId
    ) : Error(message, mapOf("qualificationId" to qualificationId.toString()))

    data class DeviceNotFound(
        override val message: String,
        val deviceId: DeviceId
    ) : Error(message, mapOf("deviceId" to deviceId.toString()))

    data class VersionConflict(override val message: String) : Error(message)
}