package cloud.fabX.fabXaccess.user.application

import assertk.assertThat
import assertk.assertions.isTrue
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import kotlin.test.BeforeTest
import kotlin.test.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.inOrder

@MockitoSettings
internal class ChangingUserTest {

    private var logger: Logger? = null
    private var testee: ChangingUser? = null

    @BeforeTest
    fun `configure DomainModule`(
        @Mock logger: Logger
    ) {
        println("@BeforeTest configure DomainModule")

        this.logger = logger
        DomainModule.configure(loggerFactory = { logger })

        testee = ChangingUser()
    }

    @Test
    fun `when changing user then it is logged`() {
        // given

        // when
        testee?.changeUser(UserIdFixture.arbitraryId(), "aFirstName")

        // then
        val inOrder = inOrder(logger!!)
        inOrder.verify(logger!!).debug("changeUser...")
        inOrder.verify(logger!!).debug("...changeUser done")
    }

    @Test
    fun `another test`() {
        assertThat(true).isTrue()
    }
}