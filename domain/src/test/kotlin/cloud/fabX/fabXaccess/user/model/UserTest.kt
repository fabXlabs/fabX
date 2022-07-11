package cloud.fabX.fabXaccess.user.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import kotlin.test.Test

internal class UserTest {

    @Test
    fun `given valid values when constructing user then user is constructed`() {
        // given
        val id = UserIdFixture.arbitraryId()

        // when
        val user = User(
            id,
            "Nikola",
            "Tesla",
            "nick",
            "00491234567890",
            false,
            null
        )

        // then
        assertThat(user).isNotNull()
        assertThat(user.id).isEqualTo(id)
    }

    @Test
    fun `given valid user when stringifying then result is correct`() {
        // given
        val user = User(
            UserIdFixture.staticId(42),
            "Nikola",
            "Tesla",
            "nick",
            "00491234567890",
            false,
            null
        )

        // when
        val result = user.toString()

        // then
        println(result)
    }
}