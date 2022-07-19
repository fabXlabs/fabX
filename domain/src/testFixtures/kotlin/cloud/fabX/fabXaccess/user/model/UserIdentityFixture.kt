package cloud.fabX.fabXaccess.user.model

import arrow.core.getOrElse

object UserIdentityFixture {

    fun usernamePassword(
        username: String = "someone",
        passwordHash: String = "4Mt7/bEfN/samfzYogSTVcnCleWI1zehSJa4HdcWsMQ="
    ): UsernamePasswordIdentity {
        return UsernamePasswordIdentity.fromUnvalidated(username, passwordHash)
            .getOrElse { throw IllegalArgumentException("Invalid username or password.") }
    }

    fun card(
        cardId: String = "AABBCCDDEEFF11",
        cardSecret: String = "F4B726CC27C2413227382ABF095D09B1A13B00FC6AD1B1B5D75C4A954628C807"
    ): CardIdentity {
        return CardIdentity.fromUnvalidated(cardId, cardSecret)
            .getOrElse { throw IllegalArgumentException("Invalid card id or secret.") }
    }

    fun phoneNr(
        phoneNr: String
    ): PhoneNrIdentity {
        return PhoneNrIdentity.fromUnvalidated(phoneNr)
            .getOrElse { throw IllegalArgumentException("Invalid phone nr.") }
    }
}