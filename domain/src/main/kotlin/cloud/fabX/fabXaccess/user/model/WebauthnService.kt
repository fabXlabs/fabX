package cloud.fabX.fabXaccess.user.model

import arrow.core.Either
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.UserId
import com.webauthn4j.authenticator.Authenticator

interface WebauthnService {
    suspend fun getChallenge(userId: UserId): Either<Error, ByteArray>

    fun parseAndValidateRegistration(
        correlationId: CorrelationId,
        challenge: ByteArray,
        attestationObject: ByteArray,
        clientDataJSON: ByteArray
    ): Either<Error, Authenticator>
}