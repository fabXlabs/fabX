package cloud.fabX.fabXaccess.common.application

import cloud.fabX.fabXaccess.user.model.UserId

/**
 * Service to handle changing user properties.
 */
class ChangingUser {

    private val log = logger()

    // TODO: handle three options
    //       - new first name (String value)
    //       - new first name (null value)
    //       - leave first name as is
    // TODO: handle other values (secondName, ...)
    fun changeUser(userId: UserId, newFirstName: String) {
        log.debug("changeUser...")
        // TODO change user's first name
        log.debug("...changeUser done")
    }
}