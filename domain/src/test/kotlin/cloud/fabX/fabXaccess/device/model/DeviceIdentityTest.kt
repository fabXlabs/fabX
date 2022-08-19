package cloud.fabX.fabXaccess.device.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.Error
import isLeft
import isRight
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource

internal class DeviceIdentityTest {

    @ParameterizedTest
    @CsvSource(
        value = [
            "30AEA467C934, 3af53192545f72d65bed2f3c34f5aed6",
            "11AA22BB3344, 755f78e6a43a7d319e5a05b4a4eaa800",
            "112233445566, d72483107032828563465358e3ca0087",
            "112233445566, 01234567890123456789012345678901"
        ]
    )
    fun `given valid mac and secret when building MacSecretIdentity then returns instance`(
        mac: String,
        secret: String
    ) {
        // when
        val result = MacSecretIdentity.fromUnvalidated(mac, secret, null)

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(MacSecretIdentity(mac, secret))
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "11223344556", // too short
            "1122334455667", // too long
            "1122334455GG", // invalid characters (G)
            "1122334455aa", // invalid characters (lowercase)
        ]
    )
    fun `given invalid mac when building MacSecretIdentity then returns error`(
        mac: String
    ) {
        // given
        val secret = "3af53192545f72d65bed2f3c34f5aed6"
        val correlationId = CorrelationIdFixture.arbitrary()

        // when
        val result = MacSecretIdentity.fromUnvalidated(mac, secret, correlationId)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.MacInvalid(
                    "Mac is invalid (has to match ^[0-9A-F]{12}\$).",
                    mac,
                    MacSecretIdentity.macRegex,
                    correlationId
                )
            )
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "0123456789012345678901234567890", // too short
            "012345678901234567890123456789012", // too long
            "0123456789012345678901234567890A", // invalid characters (uppercase)
            "0123456789012345678901234567890z", // invalid characters (z)
        ]
    )
    fun `given invalid secret when building MacSecretIdentity then returns error`(
        secret: String
    ) {
        // given
        val mac = "112233AABBCC"
        val correlationId = CorrelationIdFixture.arbitrary()

        // when
        val result = MacSecretIdentity.fromUnvalidated(mac, secret, correlationId)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.SecretInvalid(
                    "Secret is invalid (has to match ^[0-9a-f]{32}\$).",
                    secret,
                    MacSecretIdentity.secretRegex,
                    correlationId
                )
            )
    }
}