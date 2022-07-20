package cloud.fabX.fabXaccess.user.application

import arrow.core.Option
import arrow.core.flatMap
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.application.logger
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.user.model.Admin

/**
 * Service to handle changing user properties.
 */
class ChangingUser {

    private val log = logger()
    private val userRepository = DomainModule.userRepository()
    private val gettingUserByWikiName = DomainModule.gettingUserByWikiName()

    fun changePersonalInformation(
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
            .orNone()
            .tapNone { log.debug("...changePersonalInformation done") }
            .tap { log.error("...changePersonalInformation error: $it") }
    }

    fun changeLockState(
        actor: Admin,
        correlationId: CorrelationId,
        userId: UserId,
        locked: ChangeableValue<Boolean>,
        notes: ChangeableValue<String?>
    ): Option<Error> {
        log.debug("changeLockState...")

        return userRepository.getById(userId)
            .map { it.changeLockState(actor, correlationId, locked, notes) }
            .flatMap {
                userRepository.store(it)
                    .toEither { }
                    .swap()
            }
            .swap()
            .orNone()
            .tapNone { log.debug("...changeLockState done") }
            .tap { log.debug("...changeLockState error: $it") }
    }
}