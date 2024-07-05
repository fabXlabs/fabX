package cloud.fabX.fabXaccess.user.application

import arrow.core.Either
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.user.model.Admin
import cloud.fabX.fabXaccess.user.model.UserRepository
import cloud.fabX.fabXaccess.user.model.UserSourcingEvent

class GettingUserSourcingEvents(
    loggerFactory: LoggerFactory,
    private val userRepository: UserRepository
) {
    private val log: Logger = loggerFactory.invoke(this::class.java)

    suspend fun getAll(
        actor: Admin,
        correlationId: CorrelationId
    ): List<UserSourcingEvent> {
        log.debug("getAll (actor: $actor, correlationId: $correlationId)...")
        return userRepository.getSourcingEvents()
    }

    suspend fun getById(
        actor: Admin,
        correlationId: CorrelationId,
        userId: UserId
    ): Either<Error, List<UserSourcingEvent>> {
        log.debug("getById (actor: $actor, correlationId: $correlationId, userId: $userId)...")
        return userRepository.getSourcingEventsById(userId)
    }
}