package cloud.fabX.fabXaccess.user.rest

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val aggregateVersion: Long,
    val firstName: String,
    val lastName: String,
    val wikiName: String,
    val locked: Boolean,
    val notes: String?
    // TODO member/instructor qualifications?
    // TODO isAdmin?
)

fun cloud.fabX.fabXaccess.user.model.User.toRestModel() = User(
    id = id.serialize(),
    aggregateVersion = aggregateVersion,
    firstName = firstName,
    lastName = lastName,
    wikiName = wikiName,
    locked = locked,
    notes = notes
)

@Serializable
data class UserCreationDetails(
    val firstName: String,
    val lastName: String,
    val wikiName: String
)