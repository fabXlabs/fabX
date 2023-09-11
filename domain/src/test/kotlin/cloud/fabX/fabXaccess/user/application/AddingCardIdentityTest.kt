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
import cloud.fabX.fabXaccess.user.model.AdminFixture
import cloud.fabX.fabXaccess.user.model.CardIdentityAdded
import cloud.fabX.fabXaccess.user.model.GettingUserByCardId
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
internal class AddingCardIdentityTest {

    private val adminActor = AdminFixture.arbitrary()
    private val correlationId = CorrelationIdFixture.arbitrary()

    private val userId = UserIdFixture.arbitrary()

    private val fixedInstant = Clock.System.now()
    private val fixedClock = FixedClock(fixedInstant)

    private lateinit var logger: Logger
    private lateinit var userRepository: UserRepository
    private lateinit var gettingUserByCardId: GettingUserByCardId

    private lateinit var testee: AddingCardIdentity

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock userRepository: UserRepository,
        @Mock gettingUserByCardId: GettingUserByCardId
    ) {
        this.logger = logger
        this.userRepository = userRepository
        this.gettingUserByCardId = gettingUserByCardId

        testee = AddingCardIdentity({ logger }, userRepository, gettingUserByCardId, fixedClock)
    }

    @Test
    fun `given user can be found when adding identity then sourcing event is created and stored`() = runTest {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = 1)

        val cardId = "11223344556677"
        val cardSecret = "636B2D08298280E107E47B15EBE6336D6629F4FB742243DA3A792A42D5010737"

        val expectedSourcingEvent = CardIdentityAdded(
            userId,
            2,
            adminActor.id,
            fixedInstant,
            correlationId,
            cardId,
            cardSecret
        )

        whenever(userRepository.getById(userId))
            .thenReturn(user.right())

        whenever(gettingUserByCardId.getByCardId(cardId))
            .thenReturn(Error.UserNotFoundByCardId("").left())

        whenever(userRepository.store(expectedSourcingEvent))
            .thenReturn(None)

        // when
        val result = testee.addCardIdentity(
            adminActor,
            correlationId,
            userId,
            cardId,
            cardSecret
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
        val result = testee.addCardIdentity(
            adminActor,
            correlationId,
            userId,
            "77665544332211",
            "123456789012345678901234567890"
        )

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(error)
    }

    @Test
    fun `given domain error when adding identity then returns domain error`() = runTest {
        // given
        val cardId = "11223344556677"
        val cardSecret = "EE334F5E740985180C9EDAA6B5A9EB159CFB4F19427C68336D6D23D5015547CE"

        val otherUser = UserFixture.withIdentity(UserIdentityFixture.card(cardId))

        val user = UserFixture.arbitrary(userId, aggregateVersion = 1)

        val expectedDomainError = Error.CardIdAlreadyInUse("Card id is already in use.", correlationId)

        whenever(userRepository.getById(userId))
            .thenReturn(user.right())

        whenever(gettingUserByCardId.getByCardId(cardId))
            .thenReturn(otherUser.right())

        // when
        val result = testee.addCardIdentity(
            adminActor,
            correlationId,
            userId,
            cardId,
            cardSecret
        )

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(expectedDomainError)
    }

    @Test
    fun `given sourcing event cannot be stored when adding identity then returns error`() = runTest {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = 1)

        val cardId = "77665544332211"
        val cardSecret = "5A040F53492720A82247F7C5D7F6C888AB23A712DA04A8A30D0F9EE3150E80F4"

        val expectedSourcingEvent = CardIdentityAdded(
            userId,
            2,
            adminActor.id,
            fixedInstant,
            correlationId,
            cardId,
            cardSecret
        )

        val error = Error.UserNotFound("message", userId)

        whenever(userRepository.getById(userId))
            .thenReturn(user.right())

        whenever(gettingUserByCardId.getByCardId(cardId))
            .thenReturn(Error.UserNotFoundByCardId("").left())

        whenever(userRepository.store(expectedSourcingEvent))
            .thenReturn(error.some())

        // when
        val result = testee.addCardIdentity(
            adminActor,
            correlationId,
            userId,
            cardId,
            cardSecret
        )

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(error)
    }
}