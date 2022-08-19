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
import cloud.fabX.fabXaccess.common.model.ErrorFixture
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.user.model.AdminFixture
import cloud.fabX.fabXaccess.user.model.GettingUserByUsername
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import cloud.fabX.fabXaccess.user.model.UserIdentityFixture
import cloud.fabX.fabXaccess.user.model.UserRepository
import cloud.fabX.fabXaccess.user.model.UsernamePasswordIdentityAdded
import isNone
import isSome
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@MockitoSettings
internal class AddingUsernamePasswordIdentityTest {

    private val adminActor = AdminFixture.arbitrary()
    private val correlationId = CorrelationIdFixture.arbitrary()

    private val userId = UserIdFixture.arbitrary()

    private val fixedInstant = Clock.System.now()
    private val fixedClock = FixedClock(fixedInstant)

    private lateinit var logger: Logger
    private lateinit var userRepository: UserRepository
    private lateinit var gettingUserByUsername: GettingUserByUsername

    private lateinit var testee: AddingUsernamePasswordIdentity

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock userRepository: UserRepository,
        @Mock gettingUserByUsername: GettingUserByUsername
    ) {
        this.logger = logger
        this.userRepository = userRepository
        this.gettingUserByUsername = gettingUserByUsername

        testee = AddingUsernamePasswordIdentity({ logger }, userRepository, gettingUserByUsername, fixedClock)
    }

    @Test
    fun `given user can be found when adding identity then sourcing event is created and stored`() = runTest {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = 1, identities = setOf())

        val username = "username"
        val hash = "GeARB560BTUdyM8Ez2Vmld/c2F0d8vM4/Knwg8NJ9uY="

        val expectedSourcingEvent = UsernamePasswordIdentityAdded(
            userId,
            2,
            adminActor.id,
            fixedInstant,
            correlationId,
            username,
            hash
        )

        whenever(userRepository.getById(userId))
            .thenReturn(user.right())

        whenever(gettingUserByUsername.getByUsername(username))
            .thenReturn(Error.UserNotFoundByUsername("").left())

        whenever(userRepository.store(expectedSourcingEvent))
            .thenReturn(None)

        // when
        val result = testee.addUsernamePasswordIdentity(
            adminActor,
            correlationId,
            userId,
            username,
            hash
        )

        // then
        assertThat(result).isNone()

        val inOrder = inOrder(userRepository)
        inOrder.verify(userRepository).getById(userId)
        inOrder.verify(userRepository).store(expectedSourcingEvent)
    }

    @Test
    fun `given user cannot be found when adding identity then returns error`() = runTest {
        // given
        val error = Error.UserNotFound("message", userId)

        whenever(userRepository.getById(userId))
            .thenReturn(error.left())

        // when
        val result = testee.addUsernamePasswordIdentity(
            adminActor,
            correlationId,
            userId,
            "username",
            "password"
        )

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(error)
    }

    @Test
    fun `given sourcing event cannot be stored when adding identity then returns error`() = runTest {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = 1, identities = setOf())

        val username = "username"
        val hash = "6mAgYY3knb1Osh0uy10lHcBZBNcx07a37LaqtzJjMG4="

        val expectedSourcingEvent = UsernamePasswordIdentityAdded(
            userId,
            2,
            adminActor.id,
            fixedInstant,
            correlationId,
            username,
            hash
        )

        val error = ErrorFixture.arbitrary()

        whenever(userRepository.getById(userId))
            .thenReturn(user.right())

        whenever(gettingUserByUsername.getByUsername(username))
            .thenReturn(Error.UserNotFoundByUsername("").left())

        whenever(userRepository.store(expectedSourcingEvent))
            .thenReturn(error.some())

        // when
        val result = testee.addUsernamePasswordIdentity(
            adminActor,
            correlationId,
            userId,
            username,
            hash
        )

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(error)
    }

    @Test
    fun `given domain error when adding identity then returns domain error`() = runTest {
        // given
        val existingUsernamePasswordIdentity = UserIdentityFixture.usernamePassword("name")
        val user = UserFixture.arbitrary(
            userId,
            aggregateVersion = 1,
            identities = setOf(existingUsernamePasswordIdentity)
        )

        val expectedDomainError =
            Error.UsernamePasswordIdentityAlreadyFound("User already has a username password identity.", correlationId)

        whenever(userRepository.getById(userId))
            .thenReturn(user.right())

        // when
        val result = testee.addUsernamePasswordIdentity(
            adminActor,
            correlationId,
            userId,
            "username",
            "password42"
        )

        // then

        assertThat(result)
            .isSome()
            .isEqualTo(expectedDomainError)
    }
}