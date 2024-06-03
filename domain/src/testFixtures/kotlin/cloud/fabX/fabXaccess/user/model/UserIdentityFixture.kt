package cloud.fabX.fabXaccess.user.model

import arrow.core.getOrElse
import com.webauthn4j.credential.CredentialRecord
import com.webauthn4j.data.attestation.authenticator.AttestedCredentialData
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

object UserIdentityFixture {

    fun usernamePassword(
        username: String = "someone",
        passwordHash: String = "4Mt7/bEfN/samfzYogSTVcnCleWI1zehSJa4HdcWsMQ="
    ): UsernamePasswordIdentity {
        return UsernamePasswordIdentity.fromUnvalidated(username, passwordHash, null)
            .getOrElse { throw IllegalArgumentException("Invalid username or password.") }
    }

    fun card(
        cardId: String = "AABBCCDDEEFF11",
        cardSecret: String = "F4B726CC27C2413227382ABF095D09B1A13B00FC6AD1B1B5D75C4A954628C807"
    ): CardIdentity {
        return CardIdentity.fromUnvalidated(cardId, cardSecret, null)
            .getOrElse { throw IllegalArgumentException("Invalid card id or secret.") }
    }

    fun phoneNr(
        phoneNr: String
    ): PhoneNrIdentity {
        return PhoneNrIdentity.fromUnvalidated(phoneNr, null)
            .getOrElse { throw IllegalArgumentException("Invalid phone nr.") }
    }

    fun pin(
        pin: String
    ): PinIdentity {
        return PinIdentity.fromUnvalidated(pin, null)
            .getOrElse { throw IllegalArgumentException("Invalid pin.") }
    }

    fun webauthn(
        credentialId: ByteArray = byteArrayOf(1, 2, 3, 4)
    ): WebauthnIdentity {
        val authenticator = mock(CredentialRecord::class.java)
        val attestedCredentialData = mock(AttestedCredentialData::class.java)
        whenever(authenticator.attestedCredentialData).thenReturn(attestedCredentialData)
        whenever(attestedCredentialData.credentialId).thenReturn(credentialId)
        return WebauthnIdentity(authenticator)
    }
}