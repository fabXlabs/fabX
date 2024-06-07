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
import cloud.fabX.fabXaccess.user.model.Admin
import cloud.fabX.fabXaccess.user.model.AdminFixture
import cloud.fabX.fabXaccess.user.model.GettingSoftDeletedUsers
import cloud.fabX.fabXaccess.user.model.HardDeletingUser
import cloud.fabX.fabXaccess.user.model.UserDeleted
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import cloud.fabX.fabXaccess.user.model.UserRepository
import isLeft
import isRight
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.whenever

@MockitoSettings
internal class DeletingUserTest {

    private val adminActor = AdminFixture.arbitrary()
    private val correlationId = CorrelationIdFixture.arbitrary()

    private val userId = UserIdFixture.arbitrary()

    private val fixedInstant = Clock.System.now()
    private val fixedClock = FixedClock(fixedInstant)

    private lateinit var logger: Logger
    private lateinit var userRepository: UserRepository
    private lateinit var gettingSoftDeletedUsers: GettingSoftDeletedUsers
    private lateinit var hardDeletingUser: HardDeletingUser

    private lateinit var testee: DeletingUser

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock userRepository: UserRepository,
        @Mock gettingSoftDeletedUsers: GettingSoftDeletedUsers,
        @Mock hardDeletingUser: HardDeletingUser
    ) {
        this.logger = logger
        this.userRepository = userRepository
        this.gettingSoftDeletedUsers = gettingSoftDeletedUsers
        this.hardDeletingUser = hardDeletingUser

        testee = DeletingUser({ logger }, userRepository, gettingSoftDeletedUsers, hardDeletingUser, fixedClock)
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
            .thenReturn(Unit.right())

        // when
        val result = testee.deleteUser(
            adminActor,
            correlationId,
            userId
        )

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(Unit)
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
            .isLeft()
            .isEqualTo(error)
    }

    @Test
    fun `given domain error when deleting user then returns domain error`() = runTest {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = 1)
        val actor = Admin(user.id, "actor")

        whenever(userRepository.getById(userId))
            .thenReturn(user.right())

        val expectedDomainError = Error.UserIsActor("User is actor and cannot lock/delete themselves.", correlationId)

        // when
        val result = testee.deleteUser(
            actor,
            correlationId,
            userId
        )

        // then
        assertThat(result)
            .isLeft()
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
            .thenReturn(error.left())

        // when
        val result = testee.deleteUser(
            adminActor,
            correlationId,
            userId
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }

    @Test
    fun `when hard deleting user then returns unit`() = runTest {
        // given
        val user = UserFixture.arbitrary(userId)

        whenever(gettingSoftDeletedUsers.getSoftDeleted())
            .thenReturn(setOf(user))

        whenever(hardDeletingUser.hardDelete(userId))
            .thenReturn(2.right())

        // when
        val result = testee.hardDeleteUser(
            adminActor,
            correlationId,
            userId
        )

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(Unit)
    }

    @Test
    fun `given user is not soft deleted when hard deleting user then returns error`() = runTest {
        // given
        whenever(gettingSoftDeletedUsers.getSoftDeleted())
            .thenReturn(setOf())

        // when
        val result = testee.hardDeleteUser(
            adminActor,
            correlationId,
            userId
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.SoftDeletedUserNotFound(
                    "Soft deleted user not found.",
                    userId,
                    correlationId
                )
            )
    }

    @Test
    fun `given user cannot be hard deleted when hard deleting user then returns error`() = runTest {
        // given
        val expectedError = ErrorFixture.arbitrary()

        val user = UserFixture.arbitrary(userId)

        whenever(gettingSoftDeletedUsers.getSoftDeleted())
            .thenReturn(setOf(user))

        whenever(hardDeletingUser.hardDelete(userId))
            .thenReturn(expectedError.left())

        // when
        val result = testee.hardDeleteUser(
            adminActor,
            correlationId,
            userId
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(expectedError)
    }
}