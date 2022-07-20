package cloud.fabX.fabXaccess.user.application

import arrow.core.Option
import arrow.core.flatMap
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.application.logger
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.user.model.Admin
import cloud.fabX.fabXaccess.user.model.PhoneNrIdentity

/**
 * Service to add a [PhoneNrIdentity] to a user.
 */
class AddingPhoneNrIdentity {

    private val log = logger()
    private val userRepository = DomainModule.userRepository()
    private val gettingUserByIdentity = DomainModule.gettingUserByIdentity()

    fun addPhoneNrIdentity(
        actor: Admin,
        correlationId: CorrelationId,
        userId: UserId,
        phoneNr: String
    ): Option<Error> {
        log.debug("addPhoneNrIdentity...")

        return userRepository.getById(userId)
            .flatMap {
                it.addPhoneNrIdentity(
                    actor,
                    correlationId,
                    phoneNr,
                    gettingUserByIdentity
                )
            }
            .flatMap {
                userRepository.store(it)
                    .toEither { }
                    .swap()
            }
            .swap()
            .orNone()
            .tapNone { log.debug("...addPhoneNrIdentity done") }
            .tap { log.error("...addPhoneNrIdentity error: $it") }
    }
}