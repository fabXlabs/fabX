package cloud.fabX.fabXaccess.user.application

import arrow.core.Option
import arrow.core.flatMap
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
    ): Option<Error> {
        log.debug("changePersonalInformation...")

        return userRepository.getById(userId)
            .flatMap {
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
            .flatMap {
                userRepository.store(it)
                    .toEither { }
                    .swap()
            }
            .swap()
            .getOrNone()
            .onNone { log.debug("...changePersonalInformation done") }
            .onSome { log.error("...changePersonalInformation error: $it") }
    }

    suspend fun changeLockState(
        actor: Admin,
        correlationId: CorrelationId,
        userId: UserId,
        locked: ChangeableValue<Boolean>,
        notes: ChangeableValue<String?>
    ): Option<Error> {
        log.debug("changeLockState...")

        return userRepository.getById(userId)
            .flatMap { it.changeLockState(actor, clock, correlationId, locked, notes) }
            .flatMap {
                userRepository.store(it)
                    .toEither { }
                    .swap()
            }
            .swap()
            .getOrNone()
            .onNone { log.debug("...changeLockState done") }
            .onSome { log.debug("...changeLockState error: $it") }
    }
}