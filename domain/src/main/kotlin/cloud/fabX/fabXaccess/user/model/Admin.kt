package cloud.fabX.fabXaccess.user.model

import cloud.fabX.fabXaccess.common.model.Actor

/**
 * An acting administrator.
 */
data class Admin(
    val userId: UserId
) : Actor