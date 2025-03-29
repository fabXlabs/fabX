package cloud.fabX.fabXaccess.user.rest

import kotlinx.serialization.Serializable

/**
 * GDPR-compliant limited user information.
 */
@Serializable
data class LimitedUser(
    val id: String,
    val firstName: String,
    val wikiName: String
)

fun cloud.fabX.fabXaccess.user.model.LimitedUser.toRestModel() = LimitedUser(
    id = id.serialize(),
    firstName = firstName,
    wikiName = wikiName
)