package cloud.fabX.fabXaccess.device.model

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.Identity

/**
 * Identifying a Device.
 */
interface DeviceIdentity : Identity

/**
 * Identifying a Device by mac and secret.
 */
data class MacSecretIdentity(val mac: String, val secret: String) : DeviceIdentity {
    companion object {
        internal val macRegex = Regex("^[0-9A-F]{12}$")
        internal val secretRegex = Regex("^[0-9a-f]{32}$")

        fun fromUnvalidated(
            mac: String,
            secret: String,
            correlationId: CorrelationId?
        ): Either<Error, MacSecretIdentity> {
            return requireValidMac(mac, correlationId)
                .flatMap { requireValidSecret(secret, correlationId) }
                .map {
                    MacSecretIdentity(mac, secret)
                }
        }

        private fun requireValidMac(
            mac: String,
            correlationId: CorrelationId?
        ): Either<Error, Unit> {
            return if (!mac.matches(macRegex)) {
                Error.MacInvalid(
                    "Mac is invalid (has to match $macRegex).",
                    mac,
                    macRegex,
                    correlationId
                ).left()
            } else {
                Unit.right()
            }
        }

        private fun requireValidSecret(
            secret: String,
            correlationId: CorrelationId?
        ): Either<Error, Unit> {
            return if (!secret.matches(secretRegex)) {
                Error.SecretInvalid(
                    "Secret is invalid (has to match $secretRegex).",
                    secret,
                    secretRegex,
                    correlationId
                ).left()
            } else {
                Unit.right()
            }
        }
    }
}