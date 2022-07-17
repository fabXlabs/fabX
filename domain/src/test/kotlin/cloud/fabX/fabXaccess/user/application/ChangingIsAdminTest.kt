package cloud.fabX.fabXaccess.user.application

import arrow.core.None
import arrow.core.left
import arrow.core.right
import arrow.core.some
import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.model.ErrorFixture
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.user.model.AdminFixture
import cloud.fabX.fabXaccess.user.model.IsAdminChanged
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import cloud.fabX.fabXaccess.user.model.UserRepository
import isNone
import isSome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.whenever

@MockitoSettings
internal class ChangingIsAdminTest {

    private val adminActor = AdminFixture.arbitrary()

    private val userId = UserIdFixture.arbitrary()

    private var logger: Logger? = null
    private var userRepository: UserRepository? = null

    private var testee: ChangingIsAdmin? = null

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock userRepository: UserRepository
    ) {
        this.logger = logger
        this.userRepository = userRepository
        DomainModule.configureLoggerFactory { logger }
        DomainModule.configureUserRepository(userRepository)

        testee = ChangingIsAdmin()
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `given user can be found when changing is admin then sourcing is created and stored`(
        newIsAdmin: Boolean
    ) {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = 1)

        val expectedSourcingEvent = IsAdminChanged(
            userId,
            2,
            adminActor.id,
            newIsAdmin
        )

        whenever(userRepository!!.getById(userId))
            .thenReturn(user.right())

        whenever(userRepository!!.store(expectedSourcingEvent))
            .thenReturn(None)

        // when
        val result = testee!!.changeIsAdmin(
            adminActor,
            userId,
            newIsAdmin
        )

        // then
        assertThat(result).isNone()

        val inOrder = inOrder(userRepository!!)
        inOrder.verify(userRepository!!).getById(userId)
        inOrder.verify(userRepository!!).store(expectedSourcingEvent)
    }

    @Test
    fun `given user cannot be found when changing is admin then returns error`() {
        // given
        val error = ErrorFixture.arbitrary()

        whenever(userRepository!!.getById(userId))
            .thenReturn(error.left())

        // when
        val result = testee!!.changeIsAdmin(
            adminActor,
            userId,
            true
        )

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(error)
    }

    @Test
    fun `given sourcing event cannot be stored when changing is admin then returns error`() {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = 1)

        val newIsAdmin = true

        val expectedSourcingEvent = IsAdminChanged(
            userId,
            2,
            adminActor.id,
            newIsAdmin
        )

        val error = ErrorFixture.arbitrary()

        whenever(userRepository!!.getById(userId))
            .thenReturn(user.right())

        whenever(userRepository!!.store(expectedSourcingEvent))
            .thenReturn(error.some())

        // when
        val result = testee!!.changeIsAdmin(
            adminActor,
            userId,
            newIsAdmin
        )

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(error)
    }
}