package cloud.fabX.fabXaccess.user.model

import assertk.assertThat
import assertk.assertions.isNotNull
import cloud.fabX.fabXaccess.user.model.newUserId
import kotlin.test.Test

internal class UserIdTest {

    @Test
    fun `when getting new UserId then returns new instance with random UUID`() {
        // when
        val id = newUserId()

        // then
        assertThat(id).isNotNull()
        assertThat(id.value).isNotNull()
    }

}