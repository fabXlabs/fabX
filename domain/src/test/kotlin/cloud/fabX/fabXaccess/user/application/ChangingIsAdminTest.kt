package cloud.fabX.fabXaccess.user.application

import FixedClock
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.ErrorFixture
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.user.model.AdminFixture
import cloud.fabX.fabXaccess.user.model.IsAdminChanged
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import cloud.fabX.fabXaccess.user.model.UserRepository
import isLeft
import isRight
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
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
    private val correlationId = CorrelationIdFixture.arbitrary()

    private val userId = UserIdFixture.arbitrary()

    private val fixedInstant = Clock.System.now()
    private val fixedClock = FixedClock(fixedInstant)

    private lateinit var logger: Logger
    private lateinit var userRepository: UserRepository

    private lateinit var testee: ChangingIsAdmin

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock userRepository: UserRepository
    ) {
        this.logger = logger
        this.userRepository = userRepository

        testee = ChangingIsAdmin({ logger }, userRepository, fixedClock)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `given user can be found when changing is admin then sourcing is created and stored`(
        newIsAdmin: Boolean
    ) = runTest {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = 1, isAdmin = !newIsAdmin)

        val expectedSourcingEvent = IsAdminChanged(
            userId,
            2,
            adminActor.id,
            fixedInstant,
            correlationId,
            newIsAdmin
        )

        whenever(userRepository.getById(userId))
            .thenReturn(user.right())

        whenever(userRepository.store(expectedSourcingEvent))
            .thenReturn(Unit.right())

        // when
        val result = testee.changeIsAdmin(
            adminActor,
            correlationId,
            userId,
            newIsAdmin
        )

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(Unit)

        val inOrder = inOrder(userRepository)
        inOrder.verify(userRepository).getById(userId)
        inOrder.verify(userRepository).store(expectedSourcingEvent)
    }

    @Test
    fun `given user cannot be found when changing is admin then returns error`() = runTest {
        // given
        val error = ErrorFixture.arbitrary()

        whenever(userRepository.getById(userId))
            .thenReturn(error.left())

        // when
        val result = testee.changeIsAdmin(
            adminActor,
            correlationId,
            userId,
            true
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }

    @Test
    fun `given domain error when changing is admin then returns domain error`() = runTest {
        // given
        val newIsAdmin = true

        val user = UserFixture.arbitrary(userId, aggregateVersion = 1, isAdmin = newIsAdmin)

        val expectedDomainError = Error.UserAlreadyAdmin("User already is admin.", correlationId)

        whenever(userRepository.getById(userId))
            .thenReturn(user.right())

        // when
        val result = testee.changeIsAdmin(
            adminActor,
            correlationId,
            userId,
            newIsAdmin
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(expectedDomainError)
    }

    @Test
    fun `given sourcing event cannot be stored when changing is admin then returns error`() = runTest {
        // given
        val newIsAdmin = true

        val user = UserFixture.arbitrary(userId, aggregateVersion = 1, isAdmin = !newIsAdmin)

        val expectedSourcingEvent = IsAdminChanged(
            userId,
            2,
            adminActor.id,
            fixedInstant,
            correlationId,
            newIsAdmin
        )

        val error = ErrorFixture.arbitrary()

        whenever(userRepository.getById(userId))
            .thenReturn(user.right())

        whenever(userRepository.store(expectedSourcingEvent))
            .thenReturn(error.left())

        // when
        val result = testee.changeIsAdmin(
            adminActor,
            correlationId,
            userId,
            newIsAdmin
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }
}