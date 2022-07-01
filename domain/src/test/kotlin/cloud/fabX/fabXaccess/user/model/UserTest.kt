package cloud.fabX.fabXaccess.user.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import kotlin.test.Test

class UserTest {

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
}