package cloud.fabX.fabXaccess

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlin.test.assertNotNull

class AppTest {
    @Test
    fun appHasAGreeting() {
        val classUnderTest = App()
        assertNotNull(classUnderTest.greeting, "app should have a greeting")
    }

    @Test
    fun `app can use domain module`() {
        // given
        val testee = App()

        // when
        val user = testee.useDomain()

        // then
        assertThat(user.firstName).isEqualTo("first")
    }
}
