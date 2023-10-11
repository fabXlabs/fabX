package cloud.fabX.fabXaccess.user.application

import arrow.core.Option
import arrow.core.flatMap
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.user.model.Member
import cloud.fabX.fabXaccess.user.model.UserRepository
import cloud.fabX.fabXaccess.user.model.WebauthnIdentity
import cloud.fabX.fabXaccess.user.model.WebauthnService
import kotlinx.datetime.Clock

/**
 * Service to add a [WebauthnIdentity] to a user.
 */
class AddingWebauthnIdentity(
    loggerFactory: LoggerFactory,
    private val userRepository: UserRepository,
    private val webauthnService: WebauthnService,
    private val clock: Clock
) {
    private val log: Logger = loggerFactory.invoke(this::class.java)

    suspend fun addWebauthnIdentity(
        actor: Member,
        correlationId: CorrelationId,
        userId: UserId,
        attestationObject: ByteArray,
        clientDataJSON: ByteArray
    ): Option<Error> {
        log.debug("addWebauthnIdentity...")

        return webauthnService.getChallenge(userId)
            .flatMap { challenge ->
                webauthnService.parseAndValidateRegistration(
                    correlationId,
                    challenge,
                    attestationObject,
                    clientDataJSON
                )
            }
            .flatMap {
                userRepository.getById(userId)
                    .flatMap { user ->
                        user.addWebauthnIdentity(
                            actor,
                            clock,
                            correlationId,
                            it
                        )
                    }
            }
            .flatMap {
                userRepository.store(it)
                    .toEither { }
                    .swap()
            }
            .swap()
            .getOrNone()
            .onNone { log.debug("...addWebauthnIdentity done") }
            .onSome { log.error("...addWebauthnIdentity error: $it") }
    }
}