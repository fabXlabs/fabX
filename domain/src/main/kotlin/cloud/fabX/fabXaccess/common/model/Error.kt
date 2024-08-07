package cloud.fabX.fabXaccess.common.model

import cloud.fabX.fabXaccess.common.model.Error.ReferencedToolNotFound
import cloud.fabX.fabXaccess.common.model.Error.ToolNotFound
import cloud.fabX.fabXaccess.tool.model.ToolType

sealed class Error(
    open val message: String,
    open val parameters: Map<String, String> = emptyMap(),
    open val correlationId: CorrelationId? = null
) {
    data class UserNotFound(
        override val message: String,
        val userId: UserId,
        override val correlationId: CorrelationId? = null
    ) : Error(message, mapOf("userId" to userId.serialize()), correlationId)

    data class SoftDeletedUserNotFound(
        override val message: String,
        val userId: UserId,
        override val correlationId: CorrelationId? = null
    ) : Error(message, mapOf("userId" to userId.serialize()), correlationId)

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
        override val parameters: Map<String, String>,
        override val correlationId: CorrelationId
    ) : Error(message, parameters, correlationId)

    data class UserIsLocked(
        override val message: String,
        val userId: UserId,
        override val correlationId: CorrelationId? = null
    ) : Error(message, mapOf("userId" to userId.serialize()), correlationId)

    data class UsernamePasswordIdentityNotFound(
        override val message: String,
        override val correlationId: CorrelationId
    ) : Error(message)

    data class UsernamePasswordIdentityAlreadyFound(
        override val message: String,
        override val correlationId: CorrelationId
    ) : Error(message)

    data class PinIdentityAlreadyFound(
        override val message: String,
        override val correlationId: CorrelationId
    ) : Error(message)

    data class UsernameAlreadyInUse(
        override val message: String,
        override val correlationId: CorrelationId
    ) : Error(message, correlationId = correlationId)

    data class PhoneNrAlreadyInUse(
        override val message: String,
        override val correlationId: CorrelationId
    ) : Error(message)

    data class WikiNameAlreadyInUse(
        override val message: String,
        override val correlationId: CorrelationId
    ) : Error(message, correlationId = correlationId)

    data class CardIdAlreadyInUse(
        override val message: String,
        override val correlationId: CorrelationId
    ) : Error(message)

    data class CredentialIdAlreadyInUse(
        override val message: String,
        override val correlationId: CorrelationId
    ) : Error(message)

    data class UserAlreadyAdmin(
        override val message: String,
        override val correlationId: CorrelationId
    ) : Error(message, correlationId = correlationId)

    data class UserAlreadyNotAdmin(
        override val message: String,
        override val correlationId: CorrelationId
    ) : Error(message, correlationId = correlationId)

    data class UserIsActor(
        override val message: String,
        override val correlationId: CorrelationId
    ) : Error(message, correlationId = correlationId)

    data class UserNotActor(
        override val message: String,
        override val correlationId: CorrelationId
    ) : Error(message, correlationId = correlationId)

    data class UserNotInstructor(override val message: String) : Error(message)
    data class UserNotAdmin(override val message: String) : Error(message)

    data class DeviceNotActor(
        override val message: String,
        override val correlationId: CorrelationId
    ) : Error(message, correlationId = correlationId)

    data class MemberQualificationNotFound(
        override val message: String,
        val qualificationId: QualificationId,
        override val correlationId: CorrelationId
    ) : Error(message, mapOf("qualificationId" to qualificationId.serialize()))

    data class MemberQualificationAlreadyFound(
        override val message: String,
        val qualificationId: QualificationId,
        override val correlationId: CorrelationId
    ) : Error(message, mapOf("qualificationId" to qualificationId.serialize()), correlationId)

    data class InstructorQualificationNotFound(
        override val message: String,
        val qualificationId: QualificationId,
        override val correlationId: CorrelationId
    ) : Error(message, mapOf("qualificationId" to qualificationId.serialize()), correlationId)

    data class InstructorQualificationAlreadyFound(
        override val message: String,
        val qualificationId: QualificationId,
        override val correlationId: CorrelationId
    ) : Error(message, mapOf("qualificationId" to qualificationId.serialize()), correlationId)

    data class QualificationNotFound(
        override val message: String,
        val qualificationId: QualificationId
    ) : Error(message, mapOf("qualificationId" to qualificationId.serialize()))

    data class ReferencedQualificationNotFound(
        override val message: String,
        val qualificationId: QualificationId,
        override val correlationId: CorrelationId
    ) : Error(message, mapOf("qualificationId" to qualificationId.serialize()), correlationId)

    data class QualificationInUse(
        override val message: String,
        val qualificationId: QualificationId,
        val toolIds: Set<ToolId>,
        override val correlationId: CorrelationId
    ) : Error(
        message,
        mapOf(
            "qualificationId" to qualificationId.serialize(),
            "toolIds" to toolIds.joinToString()
        ),
        correlationId
    )

    data class DeviceNotFound(
        override val message: String,
        val deviceId: DeviceId
    ) : Error(message, mapOf("deviceId" to deviceId.serialize()))

    data class DeviceThumbnailNotFound(
        override val message: String,
        val deviceId: DeviceId
    ) : Error(message, mapOf("deviceId" to deviceId.serialize()))

    data class DeviceNotFoundByIdentity(
        override val message: String
    ) : Error(message)

    data class DeviceNotConnected(
        override val message: String,
        val deviceId: DeviceId,
        override val correlationId: CorrelationId
    ) : Error(message, mapOf("deviceId" to deviceId.serialize()), correlationId)

    data class DeviceTimeout(
        override val message: String,
        val deviceId: DeviceId,
        override val correlationId: CorrelationId
    ) : Error(message, mapOf("deviceId" to deviceId.serialize()), correlationId)

    data class UnexpectedDeviceResponse(
        override val message: String,
        val deviceId: DeviceId,
        val response: String,
        override val correlationId: CorrelationId
    ) : Error(message, mapOf("deviceId" to deviceId.serialize(), "response" to response), correlationId)

    data class DeviceCommunicationSerializationError(
        override val message: String
    ) : Error(message)

    data class ToolNotAttachedToDevice(
        override val message: String,
        val deviceId: DeviceId,
        val toolId: ToolId,
        override val correlationId: CorrelationId
    ) : Error(message, mapOf("deviceId" to deviceId.serialize(), "toolId" to toolId.serialize()), correlationId)

    data class ToolNotFound(
        override val message: String,
        val toolId: ToolId
    ) : Error(message, mapOf("toolId" to toolId.serialize()))

    data class ToolThumbnailNotFound(
        override val message: String,
        val toolId: ToolId
    ) : Error(message, mapOf("toolId" to toolId.serialize()))

    data class ReferencedToolNotFound(
        override val message: String,
        val toolId: ToolId,
        override val correlationId: CorrelationId
    ) : Error(message, mapOf("toolId" to toolId.serialize()), correlationId)

    data class ToolTypeNotUnlock(
        override val message: String,
        val toolId: ToolId,
        val toolType: ToolType,
        override val correlationId: CorrelationId
    ) : Error(message, mapOf("toolId" to toolId.serialize(), "toolType" to toolType.name), correlationId)

    data class PinInUse(
        override val message: String,
        val pin: Int,
        override val correlationId: CorrelationId
    ) : Error(message, mapOf("pin" to pin.toString()), correlationId)

    data class PinNotInUse(
        override val message: String,
        val pin: Int,
        override val correlationId: CorrelationId
    ) : Error(message, mapOf("pin" to pin.toString()), correlationId)

    data class VersionConflict(override val message: String) : Error(message)

    data class UserIdInvalid(
        override val message: String,
        val value: String,
        override val correlationId: CorrelationId?
    ) : Error(message, mapOf("value" to value), correlationId)

    data class UsernameInvalid(
        override val message: String,
        val value: String,
        val regex: Regex,
        override val correlationId: CorrelationId?
    ) : Error(message, mapOf("value" to value, "regex" to regex.toString()), correlationId)

    data class PasswordHashInvalid(
        override val message: String,
        val value: String,
        val regex: Regex,
        override val correlationId: CorrelationId?
    ) : Error(message, mapOf("value" to value, "regex" to regex.toString()), correlationId)

    data class CardIdInvalid(
        override val message: String,
        val value: String,
        val regex: Regex,
        override val correlationId: CorrelationId?
    ) : Error(message, mapOf("value" to value, "regex" to regex.toString()), correlationId)

    data class CardSecretInvalid(
        override val message: String,
        val value: String,
        val regex: Regex,
        override val correlationId: CorrelationId?
    ) : Error(message, mapOf("value" to value, "regex" to regex.toString()), correlationId)

    data class PhoneNrInvalid(
        override val message: String,
        val value: String,
        val regex: Regex,
        override val correlationId: CorrelationId?
    ) : Error(message, mapOf("value" to value, "regex" to regex.toString()), correlationId)

    data class PinInvalid(
        override val message: String,
        val value: String,
        val regex: Regex,
        override val correlationId: CorrelationId?
    ) : Error(message, mapOf("value" to value, "regex" to regex.toString()), correlationId)


    data class MacInvalid(
        override val message: String,
        val value: String,
        val regex: Regex,
        override val correlationId: CorrelationId?
    ) : Error(message, mapOf("value" to value, "regex" to regex.toString()), correlationId)

    data class SecretInvalid(
        override val message: String,
        val value: String,
        val regex: Regex,
        override val correlationId: CorrelationId?
    ) : Error(message, mapOf("value" to value, "regex" to regex.toString()), correlationId)

    data class ThumbnailInvalid(
        override val message: String,
        override val correlationId: CorrelationId?
    ) : Error(message, correlationId = correlationId)

    data class InstructorPermissionNotFound(
        override val message: String,
        val qualificationId: QualificationId,
        override val correlationId: CorrelationId
    ) : Error(message, mapOf("qualificationId" to qualificationId.serialize()), correlationId)

    data class NotAuthenticated(
        override val message: String,
        override val correlationId: CorrelationId? = null
    ) : Error(message, correlationId = correlationId)

    data class InvalidAuthentication(
        override val message: String,
        override val correlationId: CorrelationId? = null
    ) : Error(message, correlationId = correlationId)

    data class InvalidSecondFactor(
        override val message: String,
        override val correlationId: CorrelationId? = null
    ) : Error(message, correlationId = correlationId)

    data class WebauthnError(
        override val message: String,
        override val correlationId: CorrelationId? = null
    ) : Error(message, correlationId = correlationId)

    data class ChallengeNotFound(
        override val message: String,
        val userId: UserId,
        override val correlationId: CorrelationId? = null
    ) : Error(message, mapOf("userId" to userId.serialize()), correlationId = correlationId)
}

fun ToolNotFound.toReferencedToolNotFound(correlationId: CorrelationId): ReferencedToolNotFound {
    return ReferencedToolNotFound(
        this.message,
        this.toolId,
        correlationId
    )
}

fun Error.QualificationNotFound.toReferencedQualificationNotFound(correlationId: CorrelationId): Error.ReferencedQualificationNotFound {
    return Error.ReferencedQualificationNotFound(
        this.message,
        this.qualificationId,
        correlationId
    )
}
