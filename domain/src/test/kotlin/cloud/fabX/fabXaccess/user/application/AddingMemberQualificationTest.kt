package cloud.fabX.fabXaccess.user.application

import FixedClock
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.ErrorFixture
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.qualification.model.GettingQualificationById
import cloud.fabX.fabXaccess.qualification.model.QualificationFixture
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFixture
import cloud.fabX.fabXaccess.user.model.InstructorFixture
import cloud.fabX.fabXaccess.user.model.MemberQualificationAdded
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
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.whenever

@MockitoSettings
internal class AddingMemberQualificationTest {

    private val userId = UserIdFixture.arbitrary()
    private val qualificationId = QualificationIdFixture.arbitrary()

    private val instructorActor = InstructorFixture.arbitrary(qualifications = setOf(qualificationId))
    private val correlationId = CorrelationIdFixture.arbitrary()

    private val fixedInstant = Clock.System.now()
    private val fixedClock = FixedClock(fixedInstant)

    private lateinit var logger: Logger
    private lateinit var userRepository: UserRepository
    private lateinit var gettingQualificationById: GettingQualificationById

    private lateinit var testee: AddingMemberQualification

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock userRepository: UserRepository,
        @Mock qualificationRepository: GettingQualificationById
    ) {
        this.logger = logger
        this.userRepository = userRepository
        this.gettingQualificationById = qualificationRepository

        testee = AddingMemberQualification({ logger }, userRepository, qualificationRepository, fixedClock)
    }

    @Test
    fun `given user and qualification can be found when adding member qualification then sourcing event is created and stored`() =
        runTest {
            // given
            val user = UserFixture.arbitrary(userId, aggregateVersion = 1)

            val qualification = QualificationFixture.arbitrary(qualificationId)

            val expectedSourcingEvent = MemberQualificationAdded(
                userId,
                2,
                instructorActor.id,
                fixedInstant,
                correlationId,
                qualificationId
            )

            whenever(userRepository.getById(userId))
                .thenReturn(user.right())

            whenever(gettingQualificationById.getQualificationById(qualificationId))
                .thenReturn(qualification.right())

            whenever(userRepository.store(expectedSourcingEvent))
                .thenReturn(Unit.right())

            // when
            val result = testee.addMemberQualification(
                instructorActor,
                correlationId,
                userId,
                qualificationId
            )

            // then
            assertThat(result)
                .isRight()
                .isEqualTo(Unit)

            val inOrder = inOrder(userRepository, gettingQualificationById)
            inOrder.verify(userRepository).getById(userId)
            inOrder.verify(gettingQualificationById).getQualificationById(qualificationId)
            inOrder.verify(userRepository).store(expectedSourcingEvent)
            inOrder.verifyNoMoreInteractions()
        }

    @Test
    fun `given user cannot be found when adding member qualification then returns error`() = runTest {
        // given
        val error = ErrorFixture.arbitrary()

        whenever(userRepository.getById(userId))
            .thenReturn(error.left())

        // when
        val result = testee.addMemberQualification(
            instructorActor,
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
    fun `given qualification cannot be found when adding member qualification then returns error`() = runTest {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = 1)

        val error = ErrorFixture.arbitrary()

        whenever(userRepository.getById(userId))
            .thenReturn(user.right())

        whenever(gettingQualificationById.getQualificationById(qualificationId))
            .thenReturn(error.left())

        // when
        val result = testee.addMemberQualification(
            instructorActor,
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
    fun `given sourcing event cannot be stored when adding member qualification then returns error`() = runTest {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = 1)

        val qualification = QualificationFixture.arbitrary(qualificationId)

        val expectedSourcingEvent = MemberQualificationAdded(
            userId,
            2,
            instructorActor.id,
            fixedInstant,
            correlationId,
            qualificationId
        )

        val error = ErrorFixture.arbitrary()

        whenever(userRepository.getById(userId))
            .thenReturn(user.right())

        whenever(gettingQualificationById.getQualificationById(qualificationId))
            .thenReturn(qualification.right())

        whenever(userRepository.store(expectedSourcingEvent))
            .thenReturn(error.left())

        // when
        val result = testee.addMemberQualification(
            instructorActor,
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