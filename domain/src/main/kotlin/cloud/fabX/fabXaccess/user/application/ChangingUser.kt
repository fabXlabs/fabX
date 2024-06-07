package cloud.fabX.fabXaccess.user.application

import arrow.core.Either
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.user.model.Admin
import cloud.fabX.fabXaccess.user.model.GettingUserByWikiName
import cloud.fabX.fabXaccess.user.model.UserRepository
import kotlinx.datetime.Clock

/**
 * Service to handle changing user properties.
 */
class ChangingUser(
    loggerFactory: LoggerFactory,
    private val userRepository: UserRepository,
    private val gettingUserByWikiName: GettingUserByWikiName,
    private val clock: Clock
) {
    private val log: Logger = loggerFactory.invoke(this::class.java)

    suspend fun changePersonalInformation(
        actor: Admin,
        correlationId: CorrelationId,
        userId: UserId,
        firstName: ChangeableValue<String>,
        lastName: ChangeableValue<String>,
        wikiName: ChangeableValue<String>
    ): Either<Error, Unit> =
        userRepository.getAndStoreFlatMap(userId, actor, correlationId, log, "changePersonalInformation") {
            it.changePersonalInformation(
                actor,
                clock,
                correlationId,
                firstName,
                lastName,
                wikiName,
                gettingUserByWikiName
            )
        }

    suspend fun changeLockState(
        actor: Admin,
        correlationId: CorrelationId,
        userId: UserId,
        locked: ChangeableValue<Boolean>,
        notes: ChangeableValue<String?>
    ): Either<Error, Unit> =
        userRepository.getAndStoreFlatMap(userId, actor, correlationId, log, "changeLockState") {
            it.changeLockState(actor, clock, correlationId, locked, notes)
        }
}