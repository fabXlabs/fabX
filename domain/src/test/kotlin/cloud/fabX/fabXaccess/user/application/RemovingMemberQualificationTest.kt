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
import cloud.fabX.fabXaccess.common.model.QualificationDeleted
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFixture
import cloud.fabX.fabXaccess.user.model.AdminFixture
import cloud.fabX.fabXaccess.user.model.MemberQualificationRemoved
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.model.UserIdFixture
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
internal class RemovingMemberQualificationTest {

    private val adminActor = AdminFixture.arbitrary()
    private val correlationId = CorrelationIdFixture.arbitrary()

    private val userId = UserIdFixture.arbitrary()
    private val qualificationId = QualificationIdFixture.arbitrary()

    private val fixedInstant = Clock.System.now()
    private val fixedClock = FixedClock(fixedInstant)

    private lateinit var logger: Logger
    private lateinit var userRepository: UserRepository

    private lateinit var testee: RemovingMemberQualification

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock userRepository: UserRepository
    ) {
        this.logger = logger
        this.userRepository = userRepository

        testee = RemovingMemberQualification({ logger }, userRepository, fixedClock)
    }

    @Test
    fun `given user can be found and has qualification when removing member qualification then sourcing event is created and stored`() =
        runTest {
            // given
            val user = UserFixture.arbitrary(
                userId,
                aggregateVersion = 1,
                memberQualifications = setOf(qualificationId)
            )

            val expectedSourcingEvent = MemberQualificationRemoved(
                userId,
                2,
                adminActor.id,
                fixedInstant,
                correlationId,
                qualificationId
            )

            whenever(userRepository.getById(userId))
                .thenReturn(user.right())

            whenever(userRepository.store(expectedSourcingEvent))
                .thenReturn(Unit.right())

            // when
            val result = testee.removeMemberQualification(
                adminActor,
                correlationId,
                userId,
                qualificationId
            )

            // then
            assertThat(result)
                .isRight()
                .isEqualTo(Unit)

            val inOrder = inOrder(userRepository)
            inOrder.verify(userRepository).getById(userId)
            inOrder.verify(userRepository).store(expectedSourcingEvent)
            inOrder.verifyNoMoreInteractions()
        }

    @Test
    fun `given triggered by domain event when removing member qualification then sourcing event is created and stored`() =
        runTest {
            // given
            val user = UserFixture.arbitrary(
                userId,
                aggregateVersion = 1,
                memberQualifications = setOf(qualificationId)
            )

            val actorId = UserIdFixture.arbitrary()

            val domainEvent = QualificationDeleted(
                actorId,
                Clock.System.now(),
                correlationId,
                qualificationId
            )

            val expectedSourcingEvent = MemberQualificationRemoved(
                userId,
                2,
                actorId,
                fixedInstant,
                correlationId,
                qualificationId
            )

            whenever(userRepository.getById(userId))
                .thenReturn(user.right())

            whenever(userRepository.store(expectedSourcingEvent))
                .thenReturn(Unit.right())

            // when
            val result = testee.removeMemberQualification(
                domainEvent,
                userId,
                qualificationId
            )

            // then
            assertThat(result)
                .isRight()
                .isEqualTo(Unit)

            val inOrder = inOrder(userRepository)
            inOrder.verify(userRepository).getById(userId)
            inOrder.verify(userRepository).store(expectedSourcingEvent)
            inOrder.verifyNoMoreInteractions()
        }

    @Test
    fun `given user cannot be found when removing member qualification then returns error`() = runTest {
        // given
        val error = ErrorFixture.arbitrary()

        whenever(userRepository.getById(userId))
            .thenReturn(error.left())

        // when
        val result = testee.removeMemberQualification(
            adminActor,
            correlationId,
            userId,
            qualificationId
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }

    @Test
    fun `given user can be found but not has qualification when removing member qualification then returns error`() =
        runTest {
            // given
            val user = UserFixture.arbitrary(
                userId,
                aggregateVersion = 1,
                memberQualifications = setOf()
            )

            whenever(userRepository.getById(userId))
                .thenReturn(user.right())

            // when
            val result = testee.removeMemberQualification(
                adminActor,
                correlationId,
                userId,
                qualificationId
            )

            // then
            assertThat(result)
                .isLeft()
                .isEqualTo(
                    Error.MemberQualificationNotFound(
                        "Not able to find member qualification with id $qualificationId.",
                        qualificationId,
                        correlationId
                    )
                )
        }

    @Test
    fun `given sourcing event cannot be stored when removing member qualification then returns error`() = runTest {
        val user = UserFixture.arbitrary(
            userId,
            aggregateVersion = 1,
            memberQualifications = setOf(qualificationId)
        )

        val expectedSourcingEvent = MemberQualificationRemoved(
            userId,
            2,
            adminActor.id,
            fixedInstant,
            correlationId,
            qualificationId
        )

        val error = ErrorFixture.arbitrary()

        whenever(userRepository.getById(userId))
            .thenReturn(user.right())

        whenever(userRepository.store(expectedSourcingEvent))
            .thenReturn(error.left())

        val result = testee.removeMemberQualification(
            adminActor,
            correlationId,
            userId,
            qualificationId
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }
}