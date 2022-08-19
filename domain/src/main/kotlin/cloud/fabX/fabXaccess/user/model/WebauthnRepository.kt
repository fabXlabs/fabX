package cloud.fabX.fabXaccess.user.model

import arrow.core.Either
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.UserId

/**
 * Stores a single-use webauthn challenge per user.
 */
interface WebauthnRepository {
    /**
     * Stores the challenge for the user defined by the given id.
     */
    suspend fun storeChallenge(userId: UserId, challenge: ByteArray)

    /**
     * Deletes and returns the challenge for the user defined by the given id.
     */
    suspend fun getChallenge(userId: UserId): Either<Error, ByteArray>
}