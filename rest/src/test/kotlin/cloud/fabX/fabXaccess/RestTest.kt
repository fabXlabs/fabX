package cloud.fabX.fabXaccess

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class RestTest {

    @Test
    fun `rest can use domain module`() {
        // given
        val testee = RestClass()

        // when
        val user = testee.useDomain()

        // then
        assertThat(user.firstName).isEqualTo("first")
    }
}
