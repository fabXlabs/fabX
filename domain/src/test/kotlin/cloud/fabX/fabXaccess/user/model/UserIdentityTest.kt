package cloud.fabX.fabXaccess.user.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.Error
import isLeft
import isRight
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource

class UserIdentityTest {

    @ParameterizedTest
    @CsvSource(
        value = [
            "some.one, BxkMxFppovQgdl52KkyYDN3MGlrZn/MbTZheMJaiv/s=",
            "Another_One, h6dLJxkiB+4phuKyqiNC1JBKI+2BXYWq0R8eLiBOYIg="
        ]
    )
    fun `given valid username and hash when building UsernamePasswordIdentity then returns instance`(
        username: String,
        hash: String
    ) {
        // given

        // when
        val result = UsernamePasswordIdentity.fromUnvalidated(username, hash)

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(UsernamePasswordIdentity(username, hash))
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "user-name", // dash is illegal
            "user name", // space is illegal
            "user\nname", // newline is illegal
        ]
    )
    fun `given invalid username when building UsernamePasswordIdentity then returns error`(
        username: String
    ) {
        // given
        val hash = "h6dLJxkiB+4phuKyqiNC1JBKI+2BXYWq0R8eLiBOYIg="
        val correlationId = CorrelationIdFixture.arbitrary()

        // when
        val result = UsernamePasswordIdentity.fromUnvalidated(username, hash, correlationId)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.UsernameInvalid(
                    "Username is invalid (has to match ^[\\w.]+\$).",
                    username,
                    UsernamePasswordIdentity.usernameRegex,
                    correlationId
                )
            )
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "h6dLJxkiB+4phuKyqiNC1JBKI+2BXYWq0R8eLiBOYIg", // one character too short
            "h6dLJxkiB+4phuKyqiNC1JBKI+2BXYWq0R8eLiBOYIg_", // underscore is illegal
            "h6dLJxkiB+4phuKyqiNC1JBKI+2BXYWq0R8eLiBOYIg." // dot is illegal
        ]
    )
    fun `given invalid hash when building UsernamePasswordIdentity then returns error`(
        hash: String
    ) {
        // given
        val username = "username"
        val correlationId = CorrelationIdFixture.arbitrary()

        // when
        val result = UsernamePasswordIdentity.fromUnvalidated(username, hash, correlationId)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.PasswordHashInvalid(
                    "Password hash is invalid (has to match ^[A-Za-z0-9+/]{43}=\$).",
                    hash,
                    UsernamePasswordIdentity.hashRegex,
                    correlationId
                )
            )
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "09723319BA6E8D, D1886D05DB5A0CA7A7A2F46D8F85E14E43BBAF75977C517E9D7009BF503D971B",
            "2780919BDC6BC6, 76436695FF8B50218E6439DB99CDA1934EE5A260B2C3753FDC38146EA0D9261B",
            "7AF9FAAE28F100, 8A2808E7E5F231B3FCAD197A7CE390E086FF918688A2ACF4E85C181D6FBFF36D",
        ]
    )
    fun `given valid id and secret when building CardIdentity then returns instance`(
        cardId: String,
        cardSecret: String
    ) {
        // given

        // when
        val result = CardIdentity.fromUnvalidated(cardId, cardSecret)

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(CardIdentity(cardId, cardSecret))
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "09723319BA6E8d", // lowercase d illegal
            "09723319BA6E", // too short
            "09723319BA6EAF123", // too long
            "09723319BA6E8-" // dash illegal
        ]
    )
    fun `given invalid id when building CardIdentity then returns error`(id: String) {
        // given
        val secret = "D1886D05DB5A0CA7A7A2F46D8F85E14E43BBAF75977C517E9D7009BF503D971B"
        val correlationId = CorrelationIdFixture.arbitrary()

        // when
        val result = CardIdentity.fromUnvalidated(id, secret, correlationId)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.CardIdInvalid(
                    "Card id is invalid (has to match ^[0-9A-F]{14}\$).",
                    id,
                    CardIdentity.cardIdRegex,
                    correlationId
                )
            )
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "D1886D05DB5A0CA7A7A2F46D8F85E14E43BBAF75977C517E9D7009BF503D971b", // lowercase b illegal
            "D1886D05DB5A0CA7A7A2F46D8F85E14E43BBAF75977C517E9D7009BF503D9", // too short
            "D1886D05DB5A0CA7A7A2F46D8F85E14E43BBAF75977C517E9D7009BF503D971B111222", // too long
            "D1886D05DB5A0CA7A7A2F46D8F85E14E43BBAF75977C517E9D7009BF503D97..", // dot illegal

        ]
    )
    fun `given invalid secret when building CardIdentity then returns error`(secret: String) {
        // given
        val id = "AABBCCDDEEFF00"
        val correlationId = CorrelationIdFixture.arbitrary()

        // when
        val result = CardIdentity.fromUnvalidated(id, secret, correlationId)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.CardSecretInvalid(
                    "Card secret is invalid (has to match ^[0-9A-F]{64}\$).",
                    secret,
                    CardIdentity.cardSecretRegex,
                    correlationId
                )
            )
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "+49 123 12345678, +4912312345678",
            "+1 (567) 123 123, +1567123123"
        ]
    )
    fun `given valid phone number when building PhoneNrIdentity then returns instance`(
        phoneNr: String,
        normalized: String
    ) {
        // given

        // when
        val result = PhoneNrIdentity.fromUnvalidated(phoneNr)

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(UserIdentityFixture.phoneNr(normalized))
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "123456789", // missing plus
            "+++123456" // too many plus
        ]
    )
    fun `given invalid phone number when building PhoneNrIdentity then returns error`(
        phoneNr: String
    ) {
        // given
        val correlationId = CorrelationIdFixture.arbitrary()

        // when
        val result = PhoneNrIdentity.fromUnvalidated(phoneNr, correlationId)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.PhoneNrInvalid(
                    "Phone number is invalid (has to match ^\\+[0-9]+\$).",
                    phoneNr,
                    PhoneNrIdentity.phoneNrRegex,
                    correlationId
                )
            )
    }
}