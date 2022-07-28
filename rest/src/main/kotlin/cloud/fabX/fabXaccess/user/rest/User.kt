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
data class UsernamePasswordIdentity(
    val username: String,
    val password: String
)

@Serializable
data class CardIdentity(
    val cardId: String,
    val cardSecret: String
)