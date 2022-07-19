package cloud.fabX.fabXaccess.user.model

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
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
            .all {
                transform { it.username }.isEqualTo(username)
                transform { it.hash }.isEqualTo(hash)
            }
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "user-name", // dash is illegal
        "user name", // space is illegal
        "user\nname", // newline is illegal
    ])
    fun `given invalid username when building UsernamePasswordIdentity then returns error`(
        username: String
    ) {
        // given
        val hash = "h6dLJxkiB+4phuKyqiNC1JBKI+2BXYWq0R8eLiBOYIg="

        // when
        val result = UsernamePasswordIdentity.fromUnvalidated(username, hash)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.UsernameInvalid(
                    "Username is invalid (has to match ^[\\w.]+\$).",
                    username,
                    UsernamePasswordIdentity.usernameRegex
                )
            )
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "h6dLJxkiB+4phuKyqiNC1JBKI+2BXYWq0R8eLiBOYIg", // one character too short
        "h6dLJxkiB+4phuKyqiNC1JBKI+2BXYWq0R8eLiBOYIg_", // underscore is illegal
        "h6dLJxkiB+4phuKyqiNC1JBKI+2BXYWq0R8eLiBOYIg." // dot is illegal
    ])
    fun `given invalid hash when building UsernamePasswordIdentity then returns error`(
        hash: String
    ) {
        // given
        val username = "username"

        // when
        val result = UsernamePasswordIdentity.fromUnvalidated(username, hash)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.PasswordHashInvalid(
                    "Password hash is invalid (has to match ^[A-Za-z0-9+/]{43}=\$).",
                    hash,
                    UsernamePasswordIdentity.hashRegex
                )
            )
    }
}