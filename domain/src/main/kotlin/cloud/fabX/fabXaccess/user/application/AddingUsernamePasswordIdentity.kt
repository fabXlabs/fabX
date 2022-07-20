package cloud.fabX.fabXaccess.user.application

import arrow.core.Option
import arrow.core.flatMap
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.application.logger
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.user.model.Admin
import cloud.fabX.fabXaccess.user.model.UserId
import cloud.fabX.fabXaccess.user.model.UsernamePasswordIdentity

/**
 * Service to add a [UsernamePasswordIdentity] to a user.
 */
class AddingUsernamePasswordIdentity {

    private val log = logger()
    private val userRepository = DomainModule.userRepository()
    private val gettingUserByUsername = DomainModule.gettingUserByUsername()

    fun addUsernamePasswordIdentity(
        actor: Admin,
        correlationId: CorrelationId,
        userId: UserId,
        username: String,
        hash: String
    ): Option<Error> {
        log.debug("addUsernamePasswordIdentity...")

        return userRepository.getById(userId)
            .flatMap {
                it.addUsernamePasswordIdentity(
                    actor,
                    correlationId,
                    username,
                    hash,
                    gettingUserByUsername
                )
            }
            .flatMap {
                userRepository.store(it)
                    .toEither { }
                    .swap()
            }
            .swap()
            .orNone()
            .tapNone { log.debug("...addUsernamePasswordIdentity done") }
            .tap { log.error("...addUsernamePasswordIdentity error: $it") }
    }
}