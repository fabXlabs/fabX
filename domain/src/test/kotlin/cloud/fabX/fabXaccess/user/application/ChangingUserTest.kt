package cloud.fabX.fabXaccess.user.application

import arrow.core.None
import arrow.core.left
import arrow.core.right
import arrow.core.some
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
import isNone
import isSome
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

    private var logger: Logger? = null
    private var userRepository: UserRepository? = null
    private var gettingUserByWikiName: GettingUserByWikiName? = null

    private var testee: ChangingUser? = null

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock userRepository: UserRepository,
        @Mock gettingUserByWikiName: GettingUserByWikiName
    ) {
        this.logger = logger
        this.userRepository = userRepository
        this.gettingUserByWikiName = gettingUserByWikiName

        DomainModule.configureLoggerFactory { logger }
        DomainModule.configureUserRepository(userRepository)
        DomainModule.configureGettingUserByWikiName(gettingUserByWikiName)

        testee = ChangingUser()
    }

    @Test
    fun `given user can be found when changing personal information then sourcing event is created and stored`() {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = 1)

        val newFirstName = ChangeableValue.ChangeToValue("aFirstName")
        val newLastName = ChangeableValue.ChangeToValue("aLastName")
        val newWikiName = ChangeableValue.ChangeToValue("aWikiName")

        val expectedSourcingEvent = UserPersonalInformationChanged(
            userId,
            2,
            adminActor.id,
            correlationId,
            newFirstName,
            newLastName,
            newWikiName
        )

        whenever(userRepository!!.getById(userId))
            .thenReturn(user.right())

        whenever(gettingUserByWikiName!!.getByWikiName(newWikiName.value))
            .thenReturn(Error.UserNotFoundByWikiName("").left())

        whenever(userRepository!!.store(expectedSourcingEvent))
            .thenReturn(None)

        // when
        val result = testee!!.changePersonalInformation(
            adminActor,
            correlationId,
            userId,
            newFirstName,
            newLastName,
            newWikiName
        )

        // then
        assertThat(result).isNone()

        val inOrder = inOrder(logger!!, userRepository!!)
        inOrder.verify(logger!!).debug("changePersonalInformation...")
        inOrder.verify(userRepository!!).getById(userId)
        inOrder.verify(userRepository!!).store(expectedSourcingEvent)
        inOrder.verify(logger!!).debug("...changePersonalInformation done")
    }

    @Test
    fun `given user cannot be found when changing personal information then returns error`() {
        // given
        val error = Error.UserNotFound("message", userId)

        whenever(userRepository!!.getById(userId))
            .thenReturn(error.left())

        // when
        val result = testee!!.changePersonalInformation(
            adminActor,
            correlationId,
            userId,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs
        )

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(error)
    }

    @Test
    fun `given domain error when changing personal information then returns domain error`() {
        // given
        val newWikiName = ChangeableValue.ChangeToValue("aWikiName")

        val user = UserFixture.arbitrary(userId, aggregateVersion = 1)

        val otherUser = UserFixture.arbitrary(wikiName = newWikiName.value)

        whenever(userRepository!!.getById(userId))
            .thenReturn(user.right())

        whenever(gettingUserByWikiName!!.getByWikiName(newWikiName.value))
            .thenReturn(otherUser.right())

        val expectedDomainError = Error.WikiNameAlreadyInUse("Wiki name is already in use.")

        // when
        val result = testee!!.changePersonalInformation(
            adminActor,
            correlationId,
            userId,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs,
            newWikiName
        )

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(expectedDomainError)
    }

    @Test
    fun `given sourcing event cannot be stored when changing personal information then returns error`() {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = 1)

        val event = UserPersonalInformationChanged(
            userId,
            2,
            adminActor.id,
            correlationId,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs
        )

        val error = ErrorFixture.arbitrary()

        whenever(userRepository!!.getById(userId))
            .thenReturn(user.right())

        whenever(userRepository!!.store(event))
            .thenReturn(error.some())

        // when
        val result = testee!!.changePersonalInformation(
            adminActor,
            correlationId,
            userId,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs
        )

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(error)
    }

    @Test
    fun `given user can be found when changing lock state then sourcing event is created and stored`() {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = 3)

        val newLocked = ChangeableValue.ChangeToValue(true)
        val newNotes = ChangeableValue.ChangeToValue("some notes")

        val expectedSourcingEvent = UserLockStateChanged(
            userId,
            4,
            adminActor.id,
            correlationId,
            newLocked,
            newNotes
        )

        whenever(userRepository!!.getById(userId))
            .thenReturn(user.right())

        whenever(userRepository!!.store(eq(expectedSourcingEvent)))
            .thenReturn(None)

        // when
        val result = testee!!.changeLockState(
            adminActor,
            correlationId,
            userId,
            newLocked,
            newNotes
        )

        // then
        assertThat(result).isNone()

        val inOrder = inOrder(logger!!, userRepository!!)
        inOrder.verify(logger!!).debug("changeLockState...")
        inOrder.verify(userRepository!!).getById(userId)
        inOrder.verify(userRepository!!).store(eq(expectedSourcingEvent))
        inOrder.verify(logger!!).debug("...changeLockState done")
    }

    @Test
    fun `given user cannot be found when changing lock state then returns error`() {
        // given
        val error = Error.UserNotFound("message", userId)

        whenever(userRepository!!.getById(userId))
            .thenReturn(error.left())

        // when
        val result = testee!!.changeLockState(
            adminActor,
            correlationId,
            userId,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs
        )

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(error)
    }

    @Test
    fun `given sourcing event cannot be stored when changing lock state then returns error`() {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = 1)

        val event = UserLockStateChanged(
            userId,
            2,
            adminActor.id,
            correlationId,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs
        )

        val error = ErrorFixture.arbitrary()

        whenever(userRepository!!.getById(userId))
            .thenReturn(user.right())

        whenever(userRepository!!.store(event))
            .thenReturn(error.some())

        // when
        val result = testee!!.changeLockState(
            adminActor,
            correlationId,
            userId,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs
        )

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(error)
    }
}