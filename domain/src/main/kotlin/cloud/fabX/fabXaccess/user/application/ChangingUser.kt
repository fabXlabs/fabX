package cloud.fabX.fabXaccess.user.application

import arrow.core.Either
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.application.logger
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.user.model.UserId

/**
 * Service to handle changing user properties.
 */
class ChangingUser {

    private val log = logger()
    private val userRepository = DomainModule.userRepository()

    fun changeUser(userId: UserId, firstName: ChangeableValue<String>): Either<Error, Unit> {
        log.debug("changeUser...")

        return userRepository.getById(userId)
            .map { it.changeValues(firstName) }
            .map { userRepository.store(it) }
            .tap { log.debug("...changeUser done") }
    }
}