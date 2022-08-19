package cloud.fabX.fabXaccess.common.application

import com.webauthn4j.authenticator.Authenticator
import com.webauthn4j.data.attestation.authenticator.AAGUID
import com.webauthn4j.data.attestation.authenticator.AttestedCredentialData
import com.webauthn4j.data.attestation.authenticator.Curve
import com.webauthn4j.data.attestation.authenticator.EC2COSEKey
import com.webauthn4j.data.attestation.statement.COSEAlgorithmIdentifier
import com.webauthn4j.data.attestation.statement.COSEKeyOperation
import java.util.UUID
import kotlinx.serialization.Serializable

@Serializable
class AuthenticatorImpl(
    private val serializableAttestedCredentialData: SerializableAttestedCredentialData,
    private var myCounter: Long
) : Authenticator {
    companion object {
        fun from(
            attestedCredentialData: AttestedCredentialData,
            counter: Long
        ): AuthenticatorImpl {
            return AuthenticatorImpl(
                SerializableAttestedCredentialData.from(attestedCredentialData),
                counter
            )
        }
    }

    override fun getAttestedCredentialData(): AttestedCredentialData {
        return serializableAttestedCredentialData.to()
    }

    override fun getCounter(): Long = myCounter

    override fun setCounter(value: Long) {
        myCounter = value
    }
}

@Serializable
data class SerializableAttestedCredentialData(
    @Serializable(with = UuidSerializer::class)
    private val aaguid: UUID?,
    private val credentialId: ByteArray,
    private val coseKey: SerializableCOSEKey
) {
    companion object {
        fun from(attestedCredentialData: AttestedCredentialData): SerializableAttestedCredentialData {
            val key = when (val coseKey = attestedCredentialData.coseKey) {
                is EC2COSEKey -> SerializableCOSEKey.from(coseKey)
                else -> throw NotImplementedError()
            }

            return SerializableAttestedCredentialData(
                attestedCredentialData.aaguid.value,
                attestedCredentialData.credentialId,
                key
            )
        }
    }

    fun to(): AttestedCredentialData {
        return AttestedCredentialData(
            AAGUID(aaguid),
            credentialId,
            coseKey.to()
        )
    }
}

@Serializable
data class SerializableCOSEKey(
    private val keyId: ByteArray?,
    private val algorithm: Long?,
    private val keyOps: List<COSEKeyOperation>?,
    private val curve: Int?,
    private val x: ByteArray?,
    private val y: ByteArray?,
    private val d: ByteArray?,
) {
    companion object {
        fun from(coseKey: EC2COSEKey): SerializableCOSEKey {
            return SerializableCOSEKey(
                coseKey.keyId,
                coseKey.algorithm?.value,
                coseKey.keyOps,
                coseKey.curve?.value,
                coseKey.x,
                coseKey.y,
                coseKey.d,
            )
        }
    }

    fun to(): EC2COSEKey {
        return EC2COSEKey(
            keyId,
            algorithm?.let { COSEAlgorithmIdentifier.create(it) },
            keyOps,
            curve?.let { Curve.create(it) },
            x,
            y,
            d
        )
    }
}