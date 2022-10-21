package cloud.fabX.fabXaccess.user.rest

import cloud.fabX.fabXaccess.common.rest.ChangeableValue
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val aggregateVersion: Long,
    val firstName: String,
    val lastName: String,
    val wikiName: String,
    val locked: Boolean,
    val notes: String?,
    val identities: Set<UserIdentity>,
    val memberQualifications: Set<String>,
    val instructorQualifications: Set<String>?,
    val isAdmin: Boolean
)

fun cloud.fabX.fabXaccess.user.model.User.toRestModel() = User(
    id = id.serialize(),
    aggregateVersion = aggregateVersion,
    firstName = firstName,
    lastName = lastName,
    wikiName = wikiName,
    locked = locked,
    notes = notes,
    identities = identities.map { it.toRestModel() }.toSet(),
    memberQualifications = memberQualifications.map { it.serialize() }.toSet(),
    instructorQualifications = instructorQualifications?.map { it.serialize() }?.toSet(),
    isAdmin = isAdmin
)

@Serializable
data class UserCreationDetails(
    val firstName: String,
    val lastName: String,
    val wikiName: String
)

@Serializable
data class UserDetails(
    val firstName: ChangeableValue<String>?,
    val lastName: ChangeableValue<String>?,
    val wikiName: ChangeableValue<String>?
)

@Serializable
data class UserLockDetails(
    val locked: ChangeableValue<Boolean>?,
    val notes: ChangeableValue<String?>?
)

@Serializable
data class IsAdminDetails(
    val isAdmin: Boolean
)

@Serializable
data class QualificationAdditionDetails(
    val qualificationId: String
)

@Serializable
data class UsernamePasswordIdentityAdditionDetails(
    val username: String,
    val password: String
)

@Serializable
data class WebauthnRegistrationDetails(
    val attestation: String,
    val challenge: ByteArray,
    val rpId: String,
    val rpName: String,
    val userId: ByteArray,
    val userName: String,
    val userDisplayName: String,
    val pubKeyCredParams: List<PubKeyCredParamEntry>
)

@Serializable
data class PubKeyCredParamEntry(val type: String, val alg: Long)

@Serializable
data class WebauthnIdentityAdditionDetails(
    val attestationObject: ByteArray,
    val clientDataJSON: ByteArray
)

@Serializable
data class WebauthnLoginDetails(val username: String)

@Serializable
data class WebauthnResponseDetails(
    val userId: String,
    val credentialId: ByteArray,
    val authenticatorData: ByteArray,
    val clientDataJSON: ByteArray,
    val signature: ByteArray
)

@Serializable
data class WebauthnLoginResponse(
    val userId: String,
    val challenge: ByteArray,
    val credentialIds: List<ByteArray>
)

@Serializable
sealed class UserIdentity

@Serializable
data class UsernamePasswordIdentity(
    val username: String
) : UserIdentity()

@Suppress("ArrayInDataClass")
@Serializable
data class WebauthnIdentity(
    val credentialId: ByteArray
) : UserIdentity()

@Serializable
data class CardIdentity(
    val cardId: String,
    val cardSecret: String
) : UserIdentity()

@Serializable
data class PhoneNrIdentity(
    val phoneNr: String
) : UserIdentity()

@Serializable
data class PinIdentityAdditionDetails(
    val pin: String
)

@Suppress("CanSealedSubClassBeObject")
@Serializable
class PinIdentity : UserIdentity()

fun cloud.fabX.fabXaccess.user.model.UserIdentity.toRestModel(): UserIdentity = when (this) {
    is cloud.fabX.fabXaccess.user.model.UsernamePasswordIdentity -> UsernamePasswordIdentity(username)
    is cloud.fabX.fabXaccess.user.model.WebauthnIdentity -> WebauthnIdentity(authenticator.attestedCredentialData.credentialId)
    is cloud.fabX.fabXaccess.user.model.PhoneNrIdentity -> PhoneNrIdentity(phoneNr)
    is cloud.fabX.fabXaccess.user.model.CardIdentity -> CardIdentity(cardId, cardSecret)
    is cloud.fabX.fabXaccess.user.model.PinIdentity -> PinIdentity()
}