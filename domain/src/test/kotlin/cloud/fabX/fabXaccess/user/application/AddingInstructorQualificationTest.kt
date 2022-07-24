package cloud.fabX.fabXaccess.user.application

import arrow.core.None
import arrow.core.left
import arrow.core.right
import arrow.core.some
import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.ErrorFixture
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.qualification.model.QualificationFixture
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFixture
import cloud.fabX.fabXaccess.qualification.model.QualificationRepository
import cloud.fabX.fabXaccess.user.model.AdminFixture
import cloud.fabX.fabXaccess.user.model.InstructorQualificationAdded
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import cloud.fabX.fabXaccess.user.model.UserRepository
import isNone
import isSome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.whenever

@MockitoSettings
internal class AddingInstructorQualificationTest {

    private val adminActor = AdminFixture.arbitrary()
    private val correlationId = CorrelationIdFixture.arbitrary()

    private val userId = UserIdFixture.arbitrary()
    private val qualificationId = QualificationIdFixture.arbitrary()

    private var logger: Logger? = null
    private var userRepository: UserRepository? = null
    private var qualificationRepository: QualificationRepository? = null

    private var testee: AddingInstructorQualification? = null

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock userRepository: UserRepository,
        @Mock qualificationRepository: QualificationRepository
    ) {
        this.logger = logger
        this.userRepository = userRepository
        this.qualificationRepository = qualificationRepository

        DomainModule.configureLoggerFactory { logger }
        DomainModule.configureUserRepository(userRepository)
        DomainModule.configureQualificationRepository(qualificationRepository)

        testee = AddingInstructorQualification()
    }

    @Test
    fun `given user and qualification can be found when adding instructor qualification then sourcing event is created and stored`() {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = 1)

        val qualification = QualificationFixture.arbitrary(qualificationId)

        val expectedSourcingEvent = InstructorQualificationAdded(
            userId,
            2,
            adminActor.id,
            correlationId,
            qualificationId
        )

        whenever(userRepository!!.getById(userId))
            .thenReturn(user.right())

        whenever(qualificationRepository!!.getQualificationById(qualificationId))
            .thenReturn(qualification.right())

        whenever(userRepository!!.store(expectedSourcingEvent))
            .thenReturn(None)

        // when
        val result = testee!!.addInstructorQualification(
            adminActor,
            correlationId,
            userId,
            qualificationId
        )

        // then
        assertThat(result).isNone()

        val inOrder = inOrder(userRepository!!, qualificationRepository!!)
        inOrder.verify(userRepository!!).getById(userId)
        inOrder.verify(qualificationRepository!!).getQualificationById(qualificationId)
        inOrder.verify(userRepository!!).store(expectedSourcingEvent)
        inOrder.verifyNoMoreInteractions()
    }

    @Test
    fun `given user cannot be found when adding instructor qualification then returns error`() {
        // given
        val error = ErrorFixture.arbitrary()

        whenever(userRepository!!.getById(userId))
            .thenReturn(error.left())

        // when
        val result = testee!!.addInstructorQualification(
            adminActor,
            correlationId,
            userId,
            qualificationId
        )

        // then
        assertThat(result).isSome().isEqualTo(error)
    }

    @Test
    fun `given qualification cannot be found when adding instructor qualification then returns error`() {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = 1)

        val error = ErrorFixture.arbitrary()

        whenever(userRepository!!.getById(userId))
            .thenReturn(user.right())

        whenever(qualificationRepository!!.getQualificationById(qualificationId))
            .thenReturn(error.left())

        // when
        val result = testee!!.addInstructorQualification(
            adminActor,
            correlationId,
            userId,
            qualificationId
        )

        // then
        assertThat(result).isSome().isEqualTo(error)
    }

    @Test
    fun `given sourcing event cannot be stored when adding instructor qualification then returns error`() {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = 1)

        val qualification = QualificationFixture.arbitrary(qualificationId)

        val expectedSourcingEvent = InstructorQualificationAdded(
            userId,
            2,
            adminActor.id,
            correlationId,
            qualificationId
        )

        val error = ErrorFixture.arbitrary()

        whenever(userRepository!!.getById(userId))
            .thenReturn(user.right())

        whenever(qualificationRepository!!.getQualificationById(qualificationId))
            .thenReturn(qualification.right())

        whenever(userRepository!!.store(expectedSourcingEvent))
            .thenReturn(error.some())

        // when
        val result = testee!!.addInstructorQualification(
            adminActor,
            correlationId,
            userId,
            qualificationId
        )

        // then
        assertThat(result).isSome().isEqualTo(error)
    }
}