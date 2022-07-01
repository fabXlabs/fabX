package cloud.fabX.fabXaccess

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import cloud.fabX.fabXaccess.user.model.User
import cloud.fabX.fabXaccess.user.model.newUserId
import kotlin.test.Test

class UserTest {

    @Test
    fun `given valid values when constructing user then user is constructed`() {
        // given
        val id = newUserId()

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
}