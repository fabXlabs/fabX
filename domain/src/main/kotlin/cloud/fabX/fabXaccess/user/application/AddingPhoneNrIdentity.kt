package cloud.fabX.fabXaccess.user.application
import arrow.core.Option
import arrow.core.flatMap
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.application.logger
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.user.model.Admin
import cloud.fabX.fabXaccess.user.model.PhoneNrIdentity
import cloud.fabX.fabXaccess.user.model.UserId

/**
 * Service to add a [PhoneNrIdentity] to a user.
 */
class AddingPhoneNrIdentity {

    private val log = logger()
    private val userRepository = DomainModule.userRepository()

    fun addPhoneNrIdentity(
        actor: Admin,
        userId: UserId,
        phoneNr: String
    ): Option<Error> {
        log.debug("addPhoneNrIdentity...")

        return userRepository.getById(userId)
            .map {
                it.addPhoneNrIdentity(
                    actor,
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
            .tapNone { log.debug("...addPhoneNrIdentity done") }
            .tap { log.error("...addPhoneNrIdentity error: $it") }
    }
}