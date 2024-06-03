package cloud.fabX.fabXaccess.user.model

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.Identity
import com.webauthn4j.credential.CredentialRecord

/**
 * Identifying a User.
 */
sealed interface UserIdentity : Identity

/**
 * Identifying a User by username and password hash.
 */
data class UsernamePasswordIdentity(val username: String, val hash: String) : UserIdentity {
    companion object {
        internal val usernameRegex = Regex("^[\\w.]+$")
        internal val hashRegex = Regex("^[A-Za-z0-9+/]{43}=\$")

        fun fromUnvalidated(
            username: String,
            hash: String,
            correlationId: CorrelationId?
        ): Either<Error, UsernamePasswordIdentity> {
            return requireValidUsername(username, correlationId)
                .flatMap { requireValidHash(hash, correlationId) }
                .map { UsernamePasswordIdentity(username, hash) }
        }

        private fun requireValidUsername(
            username: String,
            correlationId: CorrelationId?
        ): Either<Error, Unit> {
            return if (!username.matches(usernameRegex)) {
                Error.UsernameInvalid(
                    "Username is invalid (has to match $usernameRegex).",
                    username,
                    usernameRegex,
                    correlationId
                ).left()
            } else {
                Unit.right()
            }
        }

        private fun requireValidHash(hash: String, correlationId: CorrelationId?): Either<Error, Unit> {
            return if (!hash.matches(hashRegex)) {
                Error.PasswordHashInvalid(
                    "Password hash is invalid (has to match $hashRegex).",
                    hash,
                    hashRegex,
                    correlationId
                ).left()
            } else {
                Unit.right()
            }
        }
    }
}

/**
 * Identifying a User by webauthn.
 */
data class WebauthnIdentity(val authenticator: CredentialRecord) : UserIdentity

/**
 * Identifying a User by card id and card secret.
 *
 * Only to be used via a Device reading the card.
 */
data class CardIdentity(val cardId: String, val cardSecret: String) : UserIdentity {
    companion object {
        internal val cardIdRegex = Regex("^[0-9A-F]{14}$")
        internal val cardSecretRegex = Regex("^[0-9A-F]{64}$")

        fun fromUnvalidated(
            cardId: String,
            cardSecret: String,
            correlationId: CorrelationId?
        ): Either<Error, CardIdentity> {
            return requireValidCardId(cardId, correlationId)
                .flatMap { requireValidCardSecret(cardSecret, correlationId) }
                .map {
                    CardIdentity(cardId, cardSecret)
                }
        }

        private fun requireValidCardId(
            cardId: String,
            correlationId: CorrelationId?
        ): Either<Error, Unit> {
            return if (!cardId.matches(cardIdRegex)) {
                Error.CardIdInvalid(
                    "Card id is invalid (has to match $cardIdRegex).",
                    cardId,
                    cardIdRegex,
                    correlationId
                ).left()
            } else {
                Unit.right()
            }
        }

        private fun requireValidCardSecret(
            cardSecret: String,
            correlationId: CorrelationId?
        ): Either<Error, Unit> {
            return if (!cardSecret.matches(cardSecretRegex)) {
                Error.CardSecretInvalid(
                    "Card secret is invalid (has to match $cardSecretRegex).",
                    cardSecret,
                    cardSecretRegex,
                    correlationId
                ).left()
            } else {
                Unit.right()
            }
        }
    }
}

/**
 * Identifying a User by phone number.
 *
 * Only to be used via a Device "reading" the phone number, e.g. by receiving a call from the number.
 */
data class PhoneNrIdentity(val phoneNr: String) : UserIdentity {
    companion object {
        internal val phoneNrRegex = Regex("^\\+[0-9]+$")

        fun fromUnvalidated(
            phoneNr: String,
            correlationId: CorrelationId?
        ): Either<Error, PhoneNrIdentity> {
            val normalized = phoneNr.filter { it.isDigit() || it == '+' }
            return requireValidPhoneNr(normalized, correlationId)
                .map {
                    PhoneNrIdentity(normalized)
                }
        }

        private fun requireValidPhoneNr(
            phoneNr: String,
            correlationId: CorrelationId?
        ): Either<Error, Unit> {
            return if (!phoneNr.matches(phoneNrRegex)) {
                Error.PhoneNrInvalid(
                    "Phone number is invalid (has to match $phoneNrRegex).",
                    phoneNr,
                    phoneNrRegex,
                    correlationId
                ).left()
            } else {
                Unit.right()
            }
        }
    }
}

data class PinIdentity(val pin: String) : UserIdentity {
    companion object {
        internal val pinRegex = Regex("^[0-9]{4}\$")

        fun fromUnvalidated(
            pin: String,
            correlationId: CorrelationId?
        ): Either<Error, PinIdentity> {
            return requireValidPin(pin, correlationId)
                .map {
                    PinIdentity(pin)
                }
        }

        private fun requireValidPin(
            pin: String,
            correlationId: CorrelationId?
        ): Either<Error, Unit> {
            return if (!pin.matches(pinRegex)) {
                Error.PinInvalid(
                    "Pin is invalid (has to match $pinRegex).",
                    pin,
                    pinRegex,
                    correlationId
                ).left()
            } else {
                Unit.right()
            }
        }
    }
}