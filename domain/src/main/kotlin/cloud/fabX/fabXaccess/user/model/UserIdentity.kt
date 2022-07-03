package cloud.fabX.fabXaccess.user.model

import cloud.fabX.fabXaccess.common.model.Identity

/**
 * Identifying a User.
 */
interface UserIdentity : Identity

/**
 * Identifying a User by username and password hash.
 */
data class UsernamePasswordIdentity(val username: String, val hash: String) : UserIdentity

/**
 * Identifying a User by card id and card secret.
 *
 * Only to be used via a Device reading the card.
 */
data class CardIdentity(val cardId: String, val cardSecret: String) : UserIdentity

/**
 * Identifying a User by phone number.
 *
 * Only to be used via a Device "reading" the phone number, e.g. by receiving a call from the number.
 */
data class PhoneNrIdentity(val phoneNr: String) : UserIdentity