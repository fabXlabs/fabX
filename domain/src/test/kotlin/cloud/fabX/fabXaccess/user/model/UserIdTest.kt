package cloud.fabX.fabXaccess.user.model

import assertk.assertThat
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.common.model.newUserId
import org.junit.jupiter.api.Test

internal class UserIdTest {

    @Test
    fun `when getting new UserId then returns new instance with random UUID`() {
        // when
        val id = newUserId()

        // then
        assertThat(id).isNotNull()
        assertThat(id.value).isNotNull()
    }

    @Test
    fun `when serializing to string and back then is equal`() {
        // given
        val id = newUserId()
        val serialized = id.serialize()
        val id2 = UserId.fromString(serialized)

        // when
        val result = (id == id2)

        // then
        assertThat(result).isTrue()
    }
}