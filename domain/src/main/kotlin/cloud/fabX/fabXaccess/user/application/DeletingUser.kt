package cloud.fabX.fabXaccess.user.application

import arrow.core.Option
import arrow.core.flatMap
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.application.logger
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.user.model.Admin
import cloud.fabX.fabXaccess.user.model.UserId

/**
 * Service to handle deleting a user.
 */
class DeletingUser {

    private val log = logger()
    private val userRepository = DomainModule.userRepository()

    fun deleteUser(
        actor: Admin,
        correlationId: CorrelationId,
        userId: UserId
    ): Option<Error> {
        log.debug("deleteUser...")

        return userRepository.getById(userId)
            .map {
                it.delete(actor, correlationId)
            }
            .flatMap {
                userRepository.store(it)
                    .toEither { }
                    .swap()
            }
            .swap()
            .orNone()
            .tapNone { log.debug("...deleteUser done") }
            .tap { log.error("...deleteUser error: $it") }
    }
}