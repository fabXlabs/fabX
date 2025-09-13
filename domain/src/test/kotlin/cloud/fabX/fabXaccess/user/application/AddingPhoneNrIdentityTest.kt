package cloud.fabX.fabXaccess.user.application

import FixedClock
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.user.model.AdminFixture
import cloud.fabX.fabXaccess.user.model.GettingUserByIdentity
import cloud.fabX.fabXaccess.user.model.PhoneNrIdentityAdded
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
internal class AddingPhoneNrIdentityTest {

    private val adminActor = AdminFixture.arbitrary()
    private val correlationId = CorrelationIdFixture.arbitrary()

    private val userId = UserIdFixture.arbitrary()

    private lateinit var logger: Logger
    private lateinit var userRepository: UserRepository
    private lateinit var gettingUserByIdentity: GettingUserByIdentity

    private val fixedInstant = Clock.System.now()
    private val fixedClock = FixedClock(fixedInstant)

    private lateinit var testee: AddingPhoneNrIdentity

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock userRepository: UserRepository,
        @Mock gettingUserByIdentity: GettingUserByIdentity
    ) {
        this.logger = logger
        this.userRepository = userRepository
        this.gettingUserByIdentity = gettingUserByIdentity

        testee = AddingPhoneNrIdentity({ logger }, userRepository, gettingUserByIdentity, fixedClock)
    }

    @Test
    fun `given user can be found when adding identity then sourcing event is created and stored`() = runTest {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = 1)

        val phoneNr = "+49123456789"

        val expectedSourcingEvent = PhoneNrIdentityAdded(
            userId,
            2,
            adminActor.id,
            fixedInstant,
            correlationId,
            phoneNr
        )

        whenever(userRepository.getById(userId))
            .thenReturn(user.right())

        whenever(gettingUserByIdentity.getByIdentity(UserIdentityFixture.phoneNr(phoneNr)))
            .thenReturn(Error.UserNotFoundByIdentity("").left())

        whenever(userRepository.store(expectedSourcingEvent))
            .thenReturn(Unit.right())

        // when
        val result = testee.addPhoneNrIdentity(
            adminActor,
            correlationId,
            userId,
            phoneNr
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
    fun `given user cannot be found when adding identity then returns error`() = runTest {
        // given
        val error = Error.UserNotFound("message", userId)

        whenever(userRepository.getById(userId))
            .thenReturn(error.left())

        // when
        val result = testee.addPhoneNrIdentity(
            adminActor,
            correlationId,
            userId,
            "+49123"
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }

    @Test
    fun `given domain error when adding identity then returns domain error`() = runTest {
        // given
        val phoneNr = "+49123456789"
        val otherUser = UserFixture.withIdentity(UserIdentityFixture.phoneNr(phoneNr))

        val user = UserFixture.arbitrary(userId, aggregateVersion = 1)

        val expectedDomainError = Error.PhoneNrAlreadyInUse(
            "Phone number is already in use.",
            correlationId
        )

        whenever(userRepository.getById(userId))
            .thenReturn(user.right())

        whenever(gettingUserByIdentity.getByIdentity(UserIdentityFixture.phoneNr(phoneNr)))
            .thenReturn(otherUser.right())

        // when
        val result = testee.addPhoneNrIdentity(
            adminActor,
            correlationId,
            userId,
            phoneNr
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(expectedDomainError)
    }

    @Test
    fun `given sourcing event cannot be stored when adding identity then returns error`() = runTest {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = 1)

        val phoneNr = "+49123456789"

        val expectedSourcingEvent = PhoneNrIdentityAdded(
            userId,
            2,
            adminActor.id,
            fixedInstant,
            correlationId,
            phoneNr
        )

        val error = Error.UserNotFound("message", userId)

        whenever(userRepository.getById(userId))
            .thenReturn(user.right())

        whenever(gettingUserByIdentity.getByIdentity(UserIdentityFixture.phoneNr(phoneNr)))
            .thenReturn(Error.UserNotFoundByIdentity("").left())

        whenever(userRepository.store(expectedSourcingEvent))
            .thenReturn(error.left())

        // when
        val result = testee.addPhoneNrIdentity(
            adminActor,
            correlationId,
            userId,
            phoneNr
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }
}