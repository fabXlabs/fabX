package cloud.fabX.fabXaccess.user.application

import arrow.core.Either
import arrow.core.flatMap
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.application.logger
import cloud.fabX.fabXaccess.common.model.Actor
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.user.model.Admin
import cloud.fabX.fabXaccess.user.model.UserId

/**
 * Service to handle changing user properties.
 */
class ChangingUser {

    private val log = logger()
    private val userRepository = DomainModule.userRepository()

    fun changePersonalInformation(
        actor: Admin,
        userId: UserId,
        firstName: ChangeableValue<String>,
        lastName: ChangeableValue<String>,
        wikiName: ChangeableValue<String>,
        phoneNumber: ChangeableValue<String?>
    ): Either<Error, Unit> {
        log.debug("changePersonalInformation...")

        return userRepository.getById(userId)
            .map {
                it.changePersonalInformation(
                    actor,
                    firstName,
                    lastName,
                    wikiName,
                    phoneNumber
                )
            }
            .flatMap {
                userRepository.store(it)
                    .toEither { }
                    .swap()
            }
            .tap { log.debug("...changePersonalInformation done") }
    }

    fun changeLockState(
        actor: Admin,
        userId: UserId,
        locked: ChangeableValue<Boolean>,
        notes: ChangeableValue<String?>
    ): Either<Error, Unit> {
        log.debug("changeLockState...")

        return userRepository.getById(userId)
            .map { it.changeLockState(actor, locked, notes) }
            .flatMap {
                userRepository.store(it)
                    .toEither { }
                    .swap()
            }
            .tap { log.debug("...changeLockState done") }
    }
}