package cloud.fabX.fabXaccess.user.application

import arrow.core.None
import arrow.core.getOrElse
import arrow.core.right
import assertk.assertThat
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.user.model.AdminFixture
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import cloud.fabX.fabXaccess.user.model.UserLockStateChanged
import cloud.fabX.fabXaccess.user.model.UserPersonalInformationChanged
import cloud.fabX.fabXaccess.user.model.UserRepository
import isRight
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.whenever

@MockitoSettings
internal class ChangingUserTest {

    private val adminActor = AdminFixture.arbitraryAdmin()

    private val userId = UserIdFixture.arbitraryId()

    private var logger: Logger? = null
    private var userRepository: UserRepository? = null

    private var testee: ChangingUser? = null

    @BeforeEach
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
    fun `given user can be found when changing user then sourcing event is created and stored`() {
        // given
        val user = UserFixture.arbitraryUser(userId, aggregateVersion = 1)

        val newFirstName = ChangeableValue.ChangeToValue("aFirstName")
        val newLastName = ChangeableValue.ChangeToValue("aLastName")
        val newWikiName = ChangeableValue.ChangeToValue("aWikiName")
        val newPhoneNumber = ChangeableValue.ChangeToValue("0123")

        val expectedSourcingEvent = UserPersonalInformationChanged(
            userId,
            2,
            adminActor.id,
            newFirstName,
            newLastName,
            newWikiName,
            newPhoneNumber
        )

        whenever(userRepository!!.getById(userId))
            .thenReturn(user.right())

        whenever(userRepository!!.store(eq(expectedSourcingEvent)))
            .thenReturn(None)

        // when
        val result = testee!!.changePersonalInformation(
            adminActor,
            userId,
            newFirstName,
            newLastName,
            newWikiName,
            newPhoneNumber
        )

        // then
        assertThat(result).isRight()

        val inOrder = inOrder(logger!!, userRepository!!)
        inOrder.verify(logger!!).debug("changePersonalInformation...")
        inOrder.verify(userRepository!!).getById(userId)
        inOrder.verify(userRepository!!).store(eq(expectedSourcingEvent))
        inOrder.verify(logger!!).debug("...changePersonalInformation done")
    }

    @Test
    fun `given user can be found when changing lock state then sourcing event is created and stored`() {
        // given
        val user = UserFixture.arbitraryUser(userId, aggregateVersion = 3)

        val newLocked = ChangeableValue.ChangeToValue(true)
        val newNotes = ChangeableValue.ChangeToValue("some notes")

        val expectedSourcingEvent = UserLockStateChanged(
            userId,
            4,
            adminActor.id,
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
            userId,
            newLocked,
            newNotes
        )

        // then
        assertThat(result).isRight()

        val inOrder = inOrder(logger!!, userRepository!!)
        inOrder.verify(logger!!).debug("changeLockState...")
        inOrder.verify(userRepository!!).getById(userId)
        inOrder.verify(userRepository!!).store(eq(expectedSourcingEvent))
        inOrder.verify(logger!!).debug("...changeLockState done")
    }
}