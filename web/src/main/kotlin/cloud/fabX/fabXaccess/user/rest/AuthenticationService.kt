package cloud.fabX.fabXaccess.user.rest

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.SystemActor
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.device.model.DeviceActor
import cloud.fabX.fabXaccess.device.model.GettingDeviceByIdentity
import cloud.fabX.fabXaccess.device.model.MacSecretIdentity
import cloud.fabX.fabXaccess.device.ws.DevicePrincipal
import cloud.fabX.fabXaccess.user.application.GettingUserByIdentity
import cloud.fabX.fabXaccess.user.model.GettingUserById
import cloud.fabX.fabXaccess.user.model.User
import cloud.fabX.fabXaccess.user.model.UsernamePasswordIdentity
import io.ktor.server.auth.Principal
import io.ktor.server.auth.UserPasswordCredential

class AuthenticationService(
    private val gettingUserByIdentity: GettingUserByIdentity,
    private val gettingUserById: GettingUserById,
    private val gettingDeviceByIdentity: GettingDeviceByIdentity
) {
    suspend fun basic(credentials: UserPasswordCredential): Principal {
        val device = MacSecretIdentity(credentials.name, credentials.password).right()
            .flatMap {
                gettingDeviceByIdentity.getByIdentity(it)
            }

        val user = UsernamePasswordIdentity
            .fromUnvalidated(
                credentials.name,
                hash(credentials.password),
                null
            )
            .flatMap {
                gettingUserByIdentity.getUserByIdentity(
                    SystemActor,
                    it
                )
            }
            .flatMap { requireUnlockedUser(it) }
            .mapLeft {
                when (it) {
                    is Error.UserNotFound -> Error.InvalidAuthentication(it.message, it.correlationId)
                    is Error.UserNotFoundByIdentity -> Error.InvalidAuthentication(it.message, it.correlationId)
                    is Error.UserNotFoundByUsername -> Error.InvalidAuthentication(it.message, it.correlationId)
                    else -> it
                }
            }
            .fold(
                { ErrorPrincipal(it) },
                { UserPrincipal(it, AuthenticationMethod.BASIC) }
            )

        return device.fold(
            { user },
            { DevicePrincipal(it) }
        )
    }

    suspend fun jwt(userId: String): Principal {
        return Either.catch { UserId.fromString(userId) }
            .mapLeft {
                Error.UserIdInvalid(
                    "UserId is invalid.",
                    userId,
                    null
                )
            }
            .flatMap { gettingUserById.getUserById(it) }
            .flatMap { requireUnlockedUser(it) }
            .fold({ ErrorPrincipal(it) }, { UserPrincipal(it) })
    }

    suspend fun augmentDeviceActorOnBehalfOfUser(
        deviceActor: DeviceActor,
        cardIdentity: CardIdentity?,
        phoneNrIdentity: PhoneNrIdentity?,
        correlationId: CorrelationId
    ): Either<Error, DeviceActor> {
        val cardUser = cardIdentity?.let {
            cloud.fabX.fabXaccess.user.model.CardIdentity.fromUnvalidated(it.cardId, it.cardSecret, correlationId)
                .flatMap { identity -> gettingUserByIdentity.getUserByIdentity(SystemActor, correlationId, identity) }
                .flatMap { user -> requireUnlockedUser(user) }
                .fold({ error ->
                    return error.left()
                }, { user ->
                    user
                })
        }

        val phoneNrUser = phoneNrIdentity?.let {
            cloud.fabX.fabXaccess.user.model.PhoneNrIdentity.fromUnvalidated(it.phoneNr, correlationId)
                .flatMap { identity -> gettingUserByIdentity.getUserByIdentity(SystemActor, correlationId, identity) }
                .flatMap { user -> requireUnlockedUser(user) }
                .fold({ error ->
                    return error.left()
                }, { user ->
                    user
                })
        }

        // assert both identities authenticate the same user
        cardUser?.let { cu ->
            phoneNrUser?.let { pu ->
                if (cu.id != pu.id) {
                    return Error.NotAuthenticated("Required authentication not found.").left()
                }
            }
        }

        val actorOnBehalfOf = phoneNrUser?.let { deviceActor.copy(onBehalfOf = it.asMember()) }
            ?: cardUser?.let { deviceActor.copy(onBehalfOf = it.asMember()) }
            ?: deviceActor

        return actorOnBehalfOf.right()
    }

    private fun requireUnlockedUser(user: User): Either<Error, User> {
        return Either.conditionally(
            !user.locked,
            { Error.UserIsLocked("User is locked.", user.id) },
            { user }
        )
    }
}