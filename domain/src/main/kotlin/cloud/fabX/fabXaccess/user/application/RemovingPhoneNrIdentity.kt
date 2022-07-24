package cloud.fabX.fabXaccess.user.application

import arrow.core.Option
import arrow.core.flatMap
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.user.model.Admin
import cloud.fabX.fabXaccess.user.model.PhoneNrIdentity
import cloud.fabX.fabXaccess.user.model.UserRepository

/**
 * Service to remove a [PhoneNrIdentity] from a user.
 */
class RemovingPhoneNrIdentity(
    loggerFactory: LoggerFactory,
    private val userRepository: UserRepository
) {
    private val log: Logger = loggerFactory.invoke(this::class.java)

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