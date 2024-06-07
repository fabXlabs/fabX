package cloud.fabX.fabXaccess.user.application

import FixedClock
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.ErrorFixture
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.user.model.AdminFixture
import cloud.fabX.fabXaccess.user.model.GettingUserByWikiName
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import cloud.fabX.fabXaccess.user.model.UserLockStateChanged
import cloud.fabX.fabXaccess.user.model.UserPersonalInformationChanged
import cloud.fabX.fabXaccess.user.model.UserRepository
import isLeft
import isRight
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.whenever

@MockitoSettings
internal class ChangingUserTest {

    private val adminActor = AdminFixture.arbitrary()
    private val correlationId = CorrelationIdFixture.arbitrary()

    private val userId = UserIdFixture.arbitrary()

    private val fixedInstant = Clock.System.now()
    private val fixedClock = FixedClock(fixedInstant)

    private lateinit var logger: Logger
    private lateinit var userRepository: UserRepository
    private lateinit var gettingUserByWikiName: GettingUserByWikiName

    private lateinit var testee: ChangingUser

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock userRepository: UserRepository,
        @Mock gettingUserByWikiName: GettingUserByWikiName
    ) {
        this.logger = logger
        this.userRepository = userRepository
        this.gettingUserByWikiName = gettingUserByWikiName

        testee = ChangingUser({ logger }, userRepository, gettingUserByWikiName, fixedClock)
    }

    @Test
    fun `given user can be found when changing personal information then sourcing event is created and stored`() =
        runTest {
            // given
            val user = UserFixture.arbitrary(userId, aggregateVersion = 1)

            val newFirstName = ChangeableValue.ChangeToValueString("aFirstName")
            val newLastName = ChangeableValue.ChangeToValueString("aLastName")
            val newWikiName = ChangeableValue.ChangeToValueString("aWikiName")

            val expectedSourcingEvent = UserPersonalInformationChanged(
                userId,
                2,
                adminActor.id,
                fixedInstant,
                correlationId,
                newFirstName,
                newLastName,
                newWikiName
            )

            whenever(userRepository.getById(userId))
                .thenReturn(user.right())

            whenever(gettingUserByWikiName.getByWikiName(newWikiName.value))
                .thenReturn(Error.UserNotFoundByWikiName("").left())

            whenever(userRepository.store(expectedSourcingEvent))
                .thenReturn(Unit.right())

            // when
            val result = testee.changePersonalInformation(
                adminActor,
                correlationId,
                userId,
                newFirstName,
                newLastName,
                newWikiName
            )

            // then
            assertThat(result)
                .isRight()
                .isEqualTo(Unit)

            val inOrder = inOrder(logger, userRepository)
            inOrder.verify(logger)
                .debug("changePersonalInformation (actor: $adminActor, correlationId: $correlationId)...")
            inOrder.verify(userRepository).getById(userId)
            inOrder.verify(userRepository).store(expectedSourcingEvent)
            inOrder.verify(logger).debug("...changePersonalInformation done")
        }

    @Test
    fun `given user cannot be found when changing personal information then returns error`() = runTest {
        // given
        val error = Error.UserNotFound("message", userId)

        whenever(userRepository.getById(userId))
            .thenReturn(error.left())

        // when
        val result = testee.changePersonalInformation(
            adminActor,
            correlationId,
            userId,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }

    @Test
    fun `given domain error when changing personal information then returns domain error`() = runTest {
        // given
        val newWikiName = ChangeableValue.ChangeToValueString("aWikiName")

        val user = UserFixture.arbitrary(userId, aggregateVersion = 1)

        val otherUser = UserFixture.arbitrary(wikiName = newWikiName.value)

        whenever(userRepository.getById(userId))
            .thenReturn(user.right())

        whenever(gettingUserByWikiName.getByWikiName(newWikiName.value))
            .thenReturn(otherUser.right())

        val expectedDomainError = Error.WikiNameAlreadyInUse("Wiki name is already in use.", correlationId)

        // when
        val result = testee.changePersonalInformation(
            adminActor,
            correlationId,
            userId,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs,
            newWikiName
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(expectedDomainError)
    }

    @Test
    fun `given sourcing event cannot be stored when changing personal information then returns error`() = runTest {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = 1)

        val event = UserPersonalInformationChanged(
            userId,
            2,
            adminActor.id,
            fixedInstant,
            correlationId,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs
        )

        val error = ErrorFixture.arbitrary()

        whenever(userRepository.getById(userId))
            .thenReturn(user.right())

        whenever(userRepository.store(event))
            .thenReturn(error.left())

        // when
        val result = testee.changePersonalInformation(
            adminActor,
            correlationId,
            userId,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }

    @Test
    fun `given user can be found when changing lock state then sourcing event is created and stored`() = runTest {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = 3)

        val newLocked = ChangeableValue.ChangeToValueBoolean(true)
        val newNotes = ChangeableValue.ChangeToValueString("some notes")

        val expectedSourcingEvent = UserLockStateChanged(
            userId,
            4,
            adminActor.id,
            fixedInstant,
            correlationId,
            newLocked,
            newNotes
        )

        whenever(userRepository.getById(userId))
            .thenReturn(user.right())

        whenever(userRepository.store(eq(expectedSourcingEvent)))
            .thenReturn(Unit.right())

        // when
        val result = testee.changeLockState(
            adminActor,
            correlationId,
            userId,
            newLocked,
            newNotes
        )

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(Unit)

        val inOrder = inOrder(logger, userRepository)
        inOrder.verify(logger).debug("changeLockState (actor: $adminActor, correlationId: $correlationId)...")
        inOrder.verify(userRepository).getById(userId)
        inOrder.verify(userRepository).store(eq(expectedSourcingEvent))
        inOrder.verify(logger).debug("...changeLockState done")
    }

    @Test
    fun `given user cannot be found when changing lock state then returns error`() = runTest {
        // given
        val error = Error.UserNotFound("message", userId)

        whenever(userRepository.getById(userId))
            .thenReturn(error.left())

        // when
        val result = testee.changeLockState(
            adminActor,
            correlationId,
            userId,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }

    @Test
    fun `given sourcing event cannot be stored when changing lock state then returns error`() = runTest {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = 1)

        val event = UserLockStateChanged(
            userId,
            2,
            adminActor.id,
            fixedInstant,
            correlationId,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs
        )

        val error = ErrorFixture.arbitrary()

        whenever(userRepository.getById(userId))
            .thenReturn(user.right())

        whenever(userRepository.store(event))
            .thenReturn(error.left())

        // when
        val result = testee.changeLockState(
            adminActor,
            correlationId,
            userId,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }
}