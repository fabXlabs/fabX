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
import cloud.fabX.fabXaccess.user.model.PasswordChanged
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import cloud.fabX.fabXaccess.user.model.UserIdentityFixture
import cloud.fabX.fabXaccess.user.model.UserRepository
import isLeft
import isRight
import kotlinx.coroutines.test.runTest
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.whenever

@OptIn(ExperimentalTime::class)
@MockitoSettings
internal class ChangingPasswordTest {

    private val correlationId = CorrelationIdFixture.arbitrary()

    private val userId = UserIdFixture.arbitrary()

    private val fixedInstant = Clock.System.now()
    private val fixedClock = FixedClock(fixedInstant)

    private lateinit var logger: Logger
    private lateinit var userRepository: UserRepository

    private lateinit var testee: ChangingPassword

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock userRepository: UserRepository
    ) {
        this.logger = logger
        this.userRepository = userRepository

        testee = ChangingPassword({ logger }, userRepository, fixedClock)
    }

    @Test
    fun `given user can be found when changing password then sourcing event is created and stored`() = runTest {
        // given
        val usernamePasswordIdentity = UserIdentityFixture.usernamePassword()
        val user = UserFixture.arbitrary(userId, aggregateVersion = 1, identities = setOf(usernamePasswordIdentity))

        val hash = "GeARB560BTUdyM8Ez2Vmld/c2F0d8vM4/Knwg8NJ9uY="

        val expectedSourcingEvent = PasswordChanged(
            userId,
            2,
            userId,
            fixedInstant,
            correlationId,
            hash
        )

        whenever(userRepository.getById(userId))
            .thenReturn(user.right())

        whenever(userRepository.store(expectedSourcingEvent))
            .thenReturn(Unit.right())

        // when
        val result = testee.changeOwnPassword(
            user.asMember(),
            correlationId,
            userId,
            hash
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
    fun `given user cannot be found when changing password then returns error`() = runTest {
        // given
        val error = Error.UserNotFound("message", userId)

        whenever(userRepository.getById(userId))
            .thenReturn(error.left())

        // when
        val result = testee.changeOwnPassword(
            UserFixture.arbitrary().asMember(),
            correlationId,
            userId,
            "hash"
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }

    @Test
    fun `given sourcing event cannot be stored when changing password then returns error`() = runTest {
        // given
        val usernamePasswordIdentity = UserIdentityFixture.usernamePassword()
        val user = UserFixture.arbitrary(userId, aggregateVersion = 1, identities = setOf(usernamePasswordIdentity))

        val hash = "GeARB560BTUdyM8Ez2Vmld/c2F0d8vM4/Knwg8NJ9uY="

        val expectedSourcingEvent = PasswordChanged(
            userId,
            2,
            userId,
            fixedInstant,
            correlationId,
            hash
        )

        val error = ErrorFixture.arbitrary()

        whenever(userRepository.getById(userId))
            .thenReturn(user.right())

        whenever(userRepository.store(expectedSourcingEvent))
            .thenReturn(error.left())

        // when
        val result = testee.changeOwnPassword(
            user.asMember(),
            correlationId,
            userId,
            hash
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }

    @Test
    fun `given domain error when changing password then returns error`() = runTest {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = 1, identities = setOf())

        val hash = "GeARB560BTUdyM8Ez2Vmld/c2F0d8vM4/Knwg8NJ9uY="

        val expectedDomainError = Error.UsernamePasswordIdentityNotFound(
            "Not able to find username password identity.",
            correlationId
        )

        whenever(userRepository.getById(userId))
            .thenReturn(user.right())

        // when
        val result = testee.changeOwnPassword(
            user.asMember(),
            correlationId,
            userId,
            hash
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(expectedDomainError)
    }
}