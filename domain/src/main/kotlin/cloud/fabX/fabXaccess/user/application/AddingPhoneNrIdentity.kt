package cloud.fabX.fabXaccess.user.application

import arrow.core.Option
import arrow.core.flatMap
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.user.model.Admin
import cloud.fabX.fabXaccess.user.model.GettingUserByIdentity
import cloud.fabX.fabXaccess.user.model.PhoneNrIdentity
import cloud.fabX.fabXaccess.user.model.UserRepository
import kotlinx.datetime.Clock

/**
 * Service to add a [PhoneNrIdentity] to a user.
 */
class AddingPhoneNrIdentity(
    loggerFactory: LoggerFactory,
    private val userRepository: UserRepository,
    private val gettingUserByIdentity: GettingUserByIdentity,
    private val clock: Clock
) {
    private val log: Logger = loggerFactory.invoke(this::class.java)

    suspend fun addPhoneNrIdentity(
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
                    clock,
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
            .getOrNone()
            .onNone { log.debug("...addPhoneNrIdentity done") }
            .onSome { log.error("...addPhoneNrIdentity error: $it") }
    }
}