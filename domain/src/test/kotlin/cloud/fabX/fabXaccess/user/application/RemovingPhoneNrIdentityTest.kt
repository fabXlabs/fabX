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
import cloud.fabX.fabXaccess.user.model.PhoneNrIdentityRemoved
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import cloud.fabX.fabXaccess.user.model.UserIdentityFixture
import cloud.fabX.fabXaccess.user.model.UserRepository
import isLeft
import isRight
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.whenever

@MockitoSettings
internal class RemovingPhoneNrIdentityTest {

    private val adminActor = AdminFixture.arbitrary()
    private val correlationId = CorrelationIdFixture.arbitrary()

    private val userId = UserIdFixture.arbitrary()

    private val fixedInstant = Clock.System.now()
    private val fixedClock = FixedClock(fixedInstant)

    private lateinit var logger: Logger
    private lateinit var userRepository: UserRepository

    private lateinit var testee: RemovingPhoneNrIdentity

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock userRepository: UserRepository
    ) {
        this.logger = logger
        this.userRepository = userRepository

        testee = RemovingPhoneNrIdentity({ logger }, userRepository, fixedClock)
    }

    @Test
    fun `given user and identity can be found when removing identity then sourcing event is created and stored`() =
        runTest {
            // given
            val phoneNr = "+491234567890"

            val user = UserFixture.arbitrary(
                userId,
                aggregateVersion = 1,
                identities = setOf(
                    UserIdentityFixture.phoneNr(phoneNr)
                )
            )

            val expectedSourcingEvent = PhoneNrIdentityRemoved(
                userId,
                2,
                adminActor.id,
                fixedInstant,
                correlationId,
                phoneNr
            )

            whenever(userRepository.getById(userId))
                .thenReturn(user.right())

            whenever(userRepository.store(expectedSourcingEvent))
                .thenReturn(Unit.right())

            // when
            val result = testee.removePhoneNrIdentity(
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
    fun `given user cannot be found when removing identity then returns error`() = runTest {
        // given
        val error = Error.UserNotFound("message", userId)

        whenever(userRepository.getById(userId))
            .thenReturn(error.left())

        // when
        val result = testee.removePhoneNrIdentity(
            adminActor,
            correlationId,
            userId,
            "username"
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }

    @Test
    fun `given sourcing event cannot be stored when removing identity then returns error`() = runTest {
        // given
        val phoneNr = "+491234567890"

        val user = UserFixture.arbitrary(
            userId,
            aggregateVersion = 1,
            identities = setOf(
                UserIdentityFixture.phoneNr(phoneNr)
            )
        )

        val expectedSourcingEvent = PhoneNrIdentityRemoved(
            userId,
            2,
            adminActor.id,
            fixedInstant,
            correlationId,
            phoneNr
        )

        val error = ErrorFixture.arbitrary()

        whenever(userRepository.getById(userId))
            .thenReturn(user.right())

        whenever(userRepository.store(expectedSourcingEvent))
            .thenReturn(error.left())

        // when
        val result = testee.removePhoneNrIdentity(
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

    @Test
    fun `given domain error when removing identity then error is returned`() = runTest {
        // given
        val user = UserFixture.arbitrary(
            userId,
            aggregateVersion = 1,
            identities = setOf()
        )

        whenever(userRepository.getById(userId))
            .thenReturn(user.right())

        // when
        val result = testee.removePhoneNrIdentity(
            adminActor,
            correlationId,
            userId,
            "+49123"
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.UserIdentityNotFound(
                    "Not able to find identity with phone number +49123.",
                    mapOf(
                        "phoneNr" to "+49123"
                    ),
                    correlationId
                )
            )
    }
}