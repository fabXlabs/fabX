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
import cloud.fabX.fabXaccess.user.model.CardIdentityRemoved
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import cloud.fabX.fabXaccess.user.model.UserIdentityFixture
import cloud.fabX.fabXaccess.user.model.UserRepository
import isNone
import isSome
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.whenever

@MockitoSettings

internal class RemovingCardIdentityTest {

    private val adminActor = AdminFixture.arbitrary()
    private val correlationId = CorrelationIdFixture.arbitrary()

    private val userId = UserIdFixture.arbitrary()

    private val fixedInstant = Clock.System.now()
    private val fixedClock = FixedClock(fixedInstant)

    private lateinit var logger: Logger
    private lateinit var userRepository: UserRepository

    private lateinit var testee: RemovingCardIdentity

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock userRepository: UserRepository
    ) {
        this.logger = logger
        this.userRepository = userRepository

        testee = RemovingCardIdentity({ logger }, userRepository, fixedClock)
    }

    @Test
    fun `given user and identity can be found when removing identity then sourcing event is created and stored`() =
        runTest {
            // given
            val cardId = "4843F377D8AF17"

            val user = UserFixture.arbitrary(
                userId,
                aggregateVersion = 1,
                identities = setOf(
                    UserIdentityFixture.card(cardId)
                )
            )

            val expectedSourcingEvent = CardIdentityRemoved(
                userId,
                2,
                adminActor.id,
                fixedInstant,
                correlationId,
                cardId
            )

            whenever(userRepository.getById(userId))
                .thenReturn(user.right())

            whenever(userRepository.store(expectedSourcingEvent))
                .thenReturn(None)

            // when
            val result = testee.removeCardIdentity(
                adminActor,
                correlationId,
                userId,
                cardId
            )

            // then
            assertThat(result).isNone()

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
        val result = testee.removeCardIdentity(
            adminActor,
            correlationId,
            userId,
            "11223344556677"
        )

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(error)
    }

    @Test
    fun `given sourcing event cannot be stored when adding identity then returns error`() = runTest {
        // given
        val cardId = "11223344556677"

        val user = UserFixture.arbitrary(
            userId,
            aggregateVersion = 1,
            identities = setOf(
                UserIdentityFixture.card(cardId)
            )
        )

        val expectedSourcingEvent = CardIdentityRemoved(
            userId,
            2,
            adminActor.id,
            fixedInstant,
            correlationId,
            cardId
        )

        val error = ErrorFixture.arbitrary()

        whenever(userRepository.getById(userId))
            .thenReturn(user.right())

        whenever(userRepository.store(expectedSourcingEvent))
            .thenReturn(error.some())

        // when
        val result = testee.removeCardIdentity(
            adminActor,
            correlationId,
            userId,
            cardId
        )

        // then
        assertThat(result)
            .isSome()
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
        val result = testee.removeCardIdentity(
            adminActor,
            correlationId,
            userId,
            "00000000000000"
        )

        // then
        assertThat(result)
            .isSome()

        val inOrder = inOrder(userRepository)
        inOrder.verify(userRepository).getById(userId)
    }
}