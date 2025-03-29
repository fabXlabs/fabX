package cloud.fabX.fabXaccess.user.model

import cloud.fabX.fabXaccess.common.model.UserId

/**
 * GDPR-compliant limited user information.
 */
data class LimitedUser(
    val id: UserId,
    val firstName: String,
    val wikiName: String
)

fun User.toLimitedUser(): LimitedUser = LimitedUser(
    id = this.id,
    firstName = this.firstName,
    wikiName = this.wikiName
)