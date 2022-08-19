package cloud.fabX.fabXaccess.user.infrastructure

import arrow.core.Either
import arrow.core.toOption
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.user.model.WebauthnRepository

class WebauthnInMemoryRepository : WebauthnRepository {
    private val data = mutableMapOf<UserId, ByteArray>()

    override suspend fun storeChallenge(userId: UserId, challenge: ByteArray) {
        data[userId] = challenge
    }

    override suspend fun getChallenge(userId: UserId): Either<Error, ByteArray> {
        return data.remove(userId)
            .toOption()
            .toEither {
                Error.ChallengeNotFound(
                    "Challenge not found for $userId.",
                    userId
                )
            }
    }
}