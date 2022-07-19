package cloud.fabX.fabXaccess.common.model

import cloud.fabX.fabXaccess.device.model.DeviceId
import cloud.fabX.fabXaccess.qualification.model.QualificationId
import cloud.fabX.fabXaccess.tool.model.ToolId
import cloud.fabX.fabXaccess.user.model.UserId

sealed class Error(open val message: String, open val parameters: Map<String, String> = emptyMap()) {
    data class UserNotFound(
        override val message: String,
        val userId: UserId
    ) : Error(message, mapOf("userId" to userId.toString()))

    data class UserNotFoundByIdentity(
        override val message: String
    ) : Error(message)

    data class UserNotFoundByUsername(
        override val message: String
    ) : Error(message)

    data class UserNotFoundByCardId(
        override val message: String
    ) : Error(message)

    data class UserNotFoundByWikiName(
        override val message: String
    ) : Error(message)

    data class UserIdentityNotFound(
        override val message: String,
        override val parameters: Map<String, String>
    ) : Error(message, parameters)

    data class UsernamePasswordIdentityAlreadyFound(
        override val message: String
    ) : Error(message)

    data class UsernameAlreadyInUse(
        override val message: String
    ) : Error(message)

    data class PhoneNrAlreadyInUse(
        override val message: String
    ) : Error(message)

    data class WikiNameAlreadyInUse(
        override val message: String
    ) : Error(message)

    data class CardIdAlreadyInUse(
        override val message: String
    ) : Error(message)

    data class UserAlreadyAdmin(
        override val message: String
    ) : Error(message)

    data class UserAlreadyNotAdmin(
        override val message: String
    ) : Error(message)

    data class UserNotInstructor(override val message: String) : Error(message)
    data class UserNotAdmin(override val message: String) : Error(message)

    data class MemberQualificationNotFound(
        override val message: String,
        val qualificationId: QualificationId
    ) : Error(message, mapOf("qualificationId" to qualificationId.toString()))

    data class MemberQualificationAlreadyFound(
        override val message: String,
        val qualificationId: QualificationId
    ) : Error(message, mapOf("qualificationId" to qualificationId.toString()))

    data class InstructorQualificationNotFound(
        override val message: String,
        val qualificationId: QualificationId
    ) : Error(message, mapOf("qualificationId" to qualificationId.toString()))

    data class InstructorQualificationAlreadyFound(
        override val message: String,
        val qualificationId: QualificationId
    ) : Error(message, mapOf("qualificationId" to qualificationId.toString()))

    data class QualificationNotFound(
        override val message: String,
        val qualificationId: QualificationId
    ) : Error(message, mapOf("qualificationId" to qualificationId.toString()))

    data class QualificationInUse(
        override val message: String,
        val qualificationId: QualificationId,
        val toolIds: Set<ToolId>
    ) : Error(
        message,
        mapOf(
            "qualificationId" to qualificationId.toString(),
            "toolIds" to toolIds.joinToString()
        )
    )

    data class DeviceNotFound(
        override val message: String,
        val deviceId: DeviceId
    ) : Error(message, mapOf("deviceId" to deviceId.toString()))

    data class DeviceNotFoundByIdentity(
        override val message: String
    ) : Error(message)

    data class ToolNotFound(
        override val message: String,
        val toolId: ToolId
    ) : Error(message, mapOf("toolId" to toolId.toString()))

    data class PinInUse(
        override val message: String,
        val pin: Int
    ) : Error(message, mapOf("pin" to pin.toString()))

    data class PinNotInUse(
        override val message: String,
        val pin: Int
    ) : Error(message, mapOf("pin" to pin.toString()))

    data class VersionConflict(override val message: String) : Error(message)

    data class UsernameInvalid(
        override val message: String,
        val value: String,
        val regex: Regex
    ) : Error(message, mapOf("value" to value, "regex" to regex.toString()))

    data class PasswordHashInvalid(
        override val message: String,
        val value: String,
        val regex: Regex
    ) : Error(message, mapOf("value" to value, "regex" to regex.toString()))

    data class CardIdInvalid(
        override val message: String,
        val value: String,
        val regex: Regex
    ) : Error(message, mapOf("value" to value, "regex" to regex.toString()))

    data class CardSecretInvalid(
        override val message: String,
        val value: String,
        val regex: Regex
    ) : Error(message, mapOf("value" to value, "regex" to regex.toString()))

    data class PhoneNrInvalid(
        override val message: String,
        val value: String,
        val regex: Regex
    ) : Error(message, mapOf("value" to value, "regex" to regex.toString()))

    data class InstructorPermissionNotFound(
        override val message: String,
        val qualificationId: QualificationId
    ) : Error(message, mapOf("qualificationId" to qualificationId.toString()))
}