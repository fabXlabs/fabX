package cloud.fabX.fabXaccess.user.application

import arrow.core.right
import assertk.assertThat
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import cloud.fabX.fabXaccess.user.model.UserRepository
import cloud.fabX.fabXaccess.user.model.UserValuesChanged
import isRight
import kotlin.test.BeforeTest
import kotlin.test.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.whenever

@MockitoSettings
internal class ChangingUserTest {

    private val userId = UserIdFixture.arbitraryId()

    private var logger: Logger? = null
    private var userRepository: UserRepository? = null

    private var testee: ChangingUser? = null

    @BeforeTest
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock userRepository: UserRepository
    ) {
        println("@BeforeTest configure DomainModule")

        this.logger = logger
        this.userRepository = userRepository
        DomainModule.configure({ logger }, userRepository)

        testee = ChangingUser()
    }

    @Test
    fun `given user can be found when changing user then SourcingEvent is created and stored`() {
        // given
        val user = UserFixture.arbitraryUser(userId)

        val newFirstName = ChangeableValue.ChangeToValue("aFirstName")
        val expectedSourcingEvent = UserValuesChanged(userId, newFirstName)

        whenever(userRepository!!.getById(userId))
            .thenReturn(user.right())

        // when
        val result = testee!!.changeUser(userId, newFirstName)

        // then
        assertThat(result).isRight()

        val inOrder = inOrder(logger!!, userRepository!!)
        inOrder.verify(logger!!).debug("changeUser...")
        inOrder.verify(userRepository!!).getById(userId)
        inOrder.verify(userRepository!!).store(eq(expectedSourcingEvent))
        inOrder.verify(logger!!).debug("...changeUser done")
    }
}