package cloud.fabX.fabXaccess.user.application

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import arrow.core.toOption
import cloud.fabX.fabXaccess.common.application.AuthenticatorImpl
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.application.toHex
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.user.model.GettingUserById
import cloud.fabX.fabXaccess.user.model.GettingUserByUsername
import cloud.fabX.fabXaccess.user.model.WebauthnIdentity
import com.webauthn4j.WebAuthnManager
import com.webauthn4j.authenticator.Authenticator
import com.webauthn4j.data.AuthenticationParameters
import com.webauthn4j.data.AuthenticationRequest
import com.webauthn4j.data.PublicKeyCredentialParameters
import com.webauthn4j.data.PublicKeyCredentialType
import com.webauthn4j.data.RegistrationParameters
import com.webauthn4j.data.RegistrationRequest
import com.webauthn4j.data.attestation.statement.COSEAlgorithmIdentifier
import com.webauthn4j.data.client.Origin
import com.webauthn4j.data.client.challenge.Challenge
import com.webauthn4j.data.client.challenge.DefaultChallenge
import com.webauthn4j.server.ServerProperty
import com.webauthn4j.validator.exception.ValidationException

// TODO define interface and move to web module
/**
 * Service to handle webauthn details.
 */
class WebauthnIdentityService(
    loggerFactory: LoggerFactory,
    private val gettingUserById: GettingUserById,
    private val gettingUserByUsername: GettingUserByUsername,
    origin: String,
    val rpId: String,
    val rpName: String
) {
    private val log: Logger = loggerFactory.invoke(this::class.java)

    private val webauthnManager = WebAuthnManager.createNonStrictWebAuthnManager()
    private val originObj = Origin(origin)

    val pubKeyCredParams = listOf(
        COSEAlgorithmIdentifier.RS1,
        COSEAlgorithmIdentifier.RS256,
        COSEAlgorithmIdentifier.RS384,
        COSEAlgorithmIdentifier.RS512,
        COSEAlgorithmIdentifier.ES256,
        COSEAlgorithmIdentifier.ES384,
        COSEAlgorithmIdentifier.ES512,
        COSEAlgorithmIdentifier.EdDSA,
    ).map {
        PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY, it)
    }

    // expectations
    private val userVerificationRequired = false
    private val userPresenceRequired = true

    suspend fun getNewChallenge(userId: UserId): Either<Error, ByteArray> {
        // TODO create new random challenge and store for specific user
        return ByteArray(8).right()
    }

    suspend fun getChallenge(userId: UserId): Either<Error, ByteArray> {
        // TODO get challenge for specific user
        return ByteArray(8).right()
    }

    fun parseAndValidateRegistration(
        correlationId: CorrelationId,
        challenge: ByteArray,
        attestationObject: ByteArray,
        clientDataJSON: ByteArray
    ): Either<Error, Authenticator> {
        val registrationRequest = RegistrationRequest(
            attestationObject,
            clientDataJSON
        )

        val challengeObj: Challenge = DefaultChallenge(challenge)
        val serverProperty = ServerProperty(originObj, rpId, challengeObj, null)

        val registrationParameters = RegistrationParameters(
            serverProperty,
            pubKeyCredParams,
            userVerificationRequired,
            userPresenceRequired
        )

        val webauthnManager = WebAuthnManager.createNonStrictWebAuthnManager()

        val registrationData = webauthnManager.parse(registrationRequest)

        try {
            webauthnManager.validate(registrationData, registrationParameters)
        } catch (e: ValidationException) {
            return Error.WebauthnError(
                e.message.toString(),
                correlationId
            ).left()
        }

        val authenticator: Authenticator = AuthenticatorImpl.from(
            registrationData.attestationObject!!.authenticatorData.attestedCredentialData!!,
            registrationData.attestationObject!!.authenticatorData.signCount
        )

        return authenticator.right()
    }

    suspend fun getLoginUserDetails(username: String): Either<Error, WebauthnLoginUserDetails> {
        return gettingUserByUsername.getByUsername(username)
            .flatMap { user ->
                val credentialIds = user.identities.filterIsInstance<WebauthnIdentity>()
                    .map { it.authenticator.attestedCredentialData.credentialId }

                getNewChallenge(user.id)
                    .map { challenge ->
                        WebauthnLoginUserDetails(
                            user.id,
                            credentialIds,
                            challenge
                        )
                    }
            }
    }

    suspend fun parseAndValidateAuthentication(
        correlationId: CorrelationId,
        userId: UserId,
        credentialId: ByteArray,
        authenticatorData: ByteArray,
        clientDataJSON: ByteArray,
        signature: ByteArray
    ): Either<Error, UserId> {
        return gettingUserById.getUserById(userId).flatMap { user ->
            user.identities.filterIsInstance<WebauthnIdentity>()
                .firstOrNull { it.authenticator.attestedCredentialData.credentialId.contentEquals(credentialId) }
                .toOption()
                .toEither {
                    Error.UserIdentityNotFound(
                        "Not able to find identity with credential id \"${credentialId.toHex()}\".",
                        mapOf("credentialId" to credentialId.toHex()),
                        correlationId
                    )
                }
                .flatMap { identity ->
                    getChallenge(userId)
                        .map { DefaultChallenge(it) }
                        .flatMap { challenge ->
                            val authenticator = identity.authenticator

                            val authenticationRequest = AuthenticationRequest(
                                credentialId,
                                authenticatorData,
                                clientDataJSON,
                                signature
                            )

                            val serverProperty = ServerProperty(originObj, rpId, challenge, null)

                            val authenticationParameters = AuthenticationParameters(
                                serverProperty,
                                authenticator,
                                listOf(credentialId),
                                userVerificationRequired,
                                userPresenceRequired
                            )

                            val authenticationData = webauthnManager.parse(authenticationRequest)

                            try {
                                webauthnManager.validate(authenticationData, authenticationParameters)

                                // TODO store updated counter
                                val counter = authenticationData.authenticatorData?.signCount
                                log.debug("new counter value: $counter")

                                user.id.right()
                            } catch (e: ValidationException) {
                                Error.NotAuthenticated("Invalid authentication.", correlationId).left()
                            }
                        }
                }
        }
    }
}

data class WebauthnLoginUserDetails(
    val userId: UserId,
    val credentialIds: List<ByteArray>,
    val challenge: ByteArray
)