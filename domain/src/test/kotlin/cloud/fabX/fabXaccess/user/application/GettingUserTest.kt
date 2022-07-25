package cloud.fabX.fabXaccess.user.application

import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isSameAs
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.ErrorFixture
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.user.model.AdminFixture
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import cloud.fabX.fabXaccess.user.model.UserRepository
import isLeft
import isRight
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.whenever

@MockitoSettings
internal class GettingUserTest {

    private val adminActor = AdminFixture.arbitrary()
    private val userId = UserIdFixture.arbitrary()
    private val correlationId = CorrelationIdFixture.arbitrary()

    private var logger: Logger? = null
    private var userRepository: UserRepository? = null

    private var testee: GettingUser? = null

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock userRepository: UserRepository
    ) {
        this.logger = logger
        this.userRepository = userRepository

        testee = GettingUser({ logger }, userRepository)
    }

    @Test
    fun `when getting all then returns all from repository`() {
        // given
        val user1 = UserFixture.arbitrary()
        val user2 = UserFixture.arbitrary()
        val user3 = UserFixture.arbitrary()

        val users = setOf(user1, user2, user3)

        whenever(userRepository!!.getAll())
            .thenReturn(users)

        // when
        val result = testee!!.getAll(adminActor, correlationId)

        // then
        assertThat(result)
            .isSameAs(users)
    }

    @Test
    fun `given user exists when getting by id then returns from repository`() {
        // given
        val user = UserFixture.arbitrary(userId)

        whenever(userRepository!!.getById(userId))
            .thenReturn(user.right())

        // when
        val result = testee!!.getById(adminActor, correlationId, userId)

        // then
        assertThat(result)
            .isRight()
            .isSameAs(user)
    }

    @Test
    fun `given repository error when getting by id then returns error`() {
        // given
        val expectedError = ErrorFixture.arbitrary()

        whenever(userRepository!!.getById(userId))
            .thenReturn(expectedError.left())

        // when
        val result = testee!!.getById(adminActor, correlationId, userId)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(expectedError)
    }
}