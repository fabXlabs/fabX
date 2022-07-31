package cloud.fabX.fabXaccess.user.application

import FixedClock
import arrow.core.None
import arrow.core.left
import arrow.core.right
import arrow.core.some
import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.user.model.Admin
import cloud.fabX.fabXaccess.user.model.AdminFixture
import cloud.fabX.fabXaccess.user.model.UserDeleted
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import cloud.fabX.fabXaccess.user.model.UserRepository
import isNone
import isSome
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@MockitoSettings
internal class DeletingUserTest {

    private val adminActor = AdminFixture.arbitrary()
    private val correlationId = CorrelationIdFixture.arbitrary()

    private val userId = UserIdFixture.arbitrary()

    private val fixedInstant = Clock.System.now()
    private val fixedClock = FixedClock(fixedInstant)

    private lateinit var logger: Logger
    private lateinit var userRepository: UserRepository

    private lateinit var testee: DeletingUser

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock userRepository: UserRepository
    ) {
        this.logger = logger
        this.userRepository = userRepository

        testee = DeletingUser({ logger }, userRepository, fixedClock)
    }

    @Test
    fun `given user can be found when deleting user then sourcing event is created and stored`() = runTest {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = 987)

        val expectedSourcingEvent = UserDeleted(
            userId,
            988,
            adminActor.id,
            fixedInstant,
            correlationId
        )

        whenever(userRepository.getById(userId))
            .thenReturn(user.right())

        whenever(userRepository.store(expectedSourcingEvent))
            .thenReturn(None)

        // when
        val result = testee.deleteUser(
            adminActor,
            correlationId,
            userId
        )

        // then
        assertThat(result).isNone()
    }

    @Test
    fun `given user cannot be found when deleting user then returns error`() = runTest {
        // given
        val error = Error.UserNotFound("message", userId)

        whenever(userRepository.getById(userId))
            .thenReturn(error.left())

        // when
        val result = testee.deleteUser(
            adminActor,
            correlationId,
            userId
        )

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(error)
    }

    @Test
    fun `given domain error when deleting user then returns domain error`() = runTest {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = 1)
        val actor = Admin(user.id, "actor")

        whenever(userRepository.getById(userId))
            .thenReturn(user.right())

        val expectedDomainError = Error.UserIsActor("User is actor and cannot delete themselves.", correlationId)

        // when
        val result = testee.deleteUser(
            actor,
            correlationId,
            userId
        )

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(expectedDomainError)
    }

    @Test
    fun `given sourcing event cannot be stored when deleting user then returns error`() = runTest {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = 987)

        val event = UserDeleted(
            userId,
            988,
            adminActor.id,
            fixedInstant,
            correlationId
        )

        val error = Error.UserNotFound("message", userId)

        whenever(userRepository.getById(userId))
            .thenReturn(user.right())

        whenever(userRepository.store(event))
            .thenReturn(error.some())

        // when
        val result = testee.deleteUser(
            adminActor,
            correlationId,
            userId
        )

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(error)
    }
}