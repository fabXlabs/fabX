package cloud.fabX.fabXaccess.user.application

import arrow.core.Option
import arrow.core.flatMap
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.application.logger
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.user.model.Admin
import cloud.fabX.fabXaccess.user.model.PhoneNrIdentity
import cloud.fabX.fabXaccess.user.model.UserId

/**
 * Service to remove a [PhoneNrIdentity] from a user.
 */
class RemovingPhoneNrIdentity {

    private val log = logger()
    private val userRepository = DomainModule.userRepository()

    fun removePhoneNrIdentity(
        actor: Admin,
        correlationId: CorrelationId,
        userId: UserId,
        phoneNr: String
    ): Option<Error> {
        log.debug("removePhoneNrIdentity...")

        return userRepository.getById(userId)
            .flatMap {
                it.removePhoneNrIdentity(
                    actor,
                    correlationId,
                    phoneNr
                )
            }
            .flatMap {
                userRepository.store(it)
                    .toEither { }
                    .swap()
            }
            .swap()
            .orNone()
            .tapNone { log.debug("...removePhoneNrIdentity done") }
            .tap { log.error("...removePhoneNrIdentity error: $it") }
    }
}