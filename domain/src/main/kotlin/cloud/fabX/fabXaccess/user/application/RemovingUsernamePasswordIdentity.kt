package cloud.fabX.fabXaccess.user.application

import arrow.core.Option
import arrow.core.flatMap
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.application.logger
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.user.model.Admin
import cloud.fabX.fabXaccess.user.model.UsernamePasswordIdentity

/**
 * Service to remove a [UsernamePasswordIdentity] from a user.
 */
class RemovingUsernamePasswordIdentity {

    private val log = logger()
    private val userRepository = DomainModule.userRepository()

    fun removeUsernamePasswordIdentity(
        actor: Admin,
        correlationId: CorrelationId,
        userId: UserId,
        username: String
    ): Option<Error> {
        log.debug("removeUsernamePasswordIdentity...")

        return userRepository.getById(userId)
            .flatMap {
                it.removeUsernamePasswordIdentity(
                    actor,
                    correlationId,
                    username
                )
            }
            .flatMap {
                userRepository.store(it)
                    .toEither { }
                    .swap()
            }
            .swap()
            .orNone()
            .tapNone { log.debug("...removeUsernamePasswordIdentity done") }
            .tap { log.error("...removeUsernamePasswordIdentity error: $it") }
    }
}