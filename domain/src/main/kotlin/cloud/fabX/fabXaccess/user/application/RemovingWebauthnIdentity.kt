package cloud.fabX.fabXaccess.user.application

import arrow.core.Either
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.user.model.Admin
import cloud.fabX.fabXaccess.user.model.Member
import cloud.fabX.fabXaccess.user.model.UserRepository
import cloud.fabX.fabXaccess.user.model.WebauthnIdentity
import kotlinx.datetime.Clock

/**
 * Service to remove a [WebauthnIdentity] to a user.
 */
class RemovingWebauthnIdentity(
    loggerFactory: LoggerFactory,
    private val userRepository: UserRepository,
    private val clock: Clock
) {
    private val log: Logger = loggerFactory.invoke(this::class.java)

    suspend fun removeWebauthnIdentity(
        actor: Admin,
        correlationId: CorrelationId,
        userId: UserId,
        credentialId: ByteArray
    ): Either<Error, Unit> =
        userRepository.getAndStoreFlatMap(userId, actor, correlationId, log, "removeWebauthnIdentity") {
            it.removeWebauthnIdentity(
                actor,
                clock,
                correlationId,
                credentialId
            )
        }

    suspend fun removeWebauthnIdentity(
        actor: Member,
        correlationId: CorrelationId,
        userId: UserId,
        credentialId: ByteArray
    ): Either<Error, Unit> =
        userRepository.getAndStoreFlatMap(userId, actor, correlationId, log, "removeWebauthnIdentity") {
            it.removeWebauthnIdentity(
                actor,
                clock,
                correlationId,
                credentialId
            )
        }
}