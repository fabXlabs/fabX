package cloud.fabX.fabXaccess.qualification.application

import FixedClock
import arrow.core.None
import arrow.core.left
import arrow.core.right
import arrow.core.some
import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.DomainEventPublisher
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.ErrorFixture
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.qualification.model.QualificationDeleted
import cloud.fabX.fabXaccess.qualification.model.QualificationFixture
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFixture
import cloud.fabX.fabXaccess.qualification.model.QualificationRepository
import cloud.fabX.fabXaccess.tool.model.GettingToolsByQualificationId
import cloud.fabX.fabXaccess.tool.model.ToolFixture
import cloud.fabX.fabXaccess.tool.model.ToolIdFixture
import cloud.fabX.fabXaccess.user.model.AdminFixture
import isNone
import isSome
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@MockitoSettings
internal class DeletingQualificationTest {

    private val adminActor = AdminFixture.arbitrary()
    private val correlationId = CorrelationIdFixture.arbitrary()

    private val qualificationId = QualificationIdFixture.arbitrary()
    private val fixedInstant = Clock.System.now()
    private val fixedClock = FixedClock(fixedInstant)

    private lateinit var logger: Logger
    private lateinit var domainEventPublisher: DomainEventPublisher
    private lateinit var qualificationRepository: QualificationRepository
    private lateinit var gettingToolsByQualificationId: GettingToolsByQualificationId

    private lateinit var testee: DeletingQualification

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock domainEventPublisher: DomainEventPublisher,
        @Mock qualificationRepository: QualificationRepository,
        @Mock gettingToolsByQualificationId: GettingToolsByQualificationId
    ) {
        this.logger = logger
        this.domainEventPublisher = domainEventPublisher
        this.qualificationRepository = qualificationRepository
        this.gettingToolsByQualificationId = gettingToolsByQualificationId

        testee = DeletingQualification(
            { logger },
            fixedClock,
            domainEventPublisher,
            qualificationRepository,
            gettingToolsByQualificationId
        )
    }

    @Test
    fun `given qualification can be found when deleting qualification then sourcing event is created and stored and domain event is published`() =
        runTest {
            // given
            val qualification = QualificationFixture.arbitrary(qualificationId, aggregateVersion = 123)

            val expectedSourcingEvent = QualificationDeleted(
                qualificationId,
                124,
                adminActor.id,
                fixedInstant,
                correlationId
            )

            whenever(qualificationRepository.getById(qualificationId))
                .thenReturn(qualification.right())

            whenever(gettingToolsByQualificationId.getToolsByQualificationId(qualificationId))
                .thenReturn(setOf())

            whenever(qualificationRepository.store(expectedSourcingEvent))
                .thenReturn(None)

            // when
            val result = testee.deleteQualification(
                adminActor,
                correlationId,
                qualificationId
            )

            // then
            assertThat(result).isNone()
            verify(domainEventPublisher).publish(
                cloud.fabX.fabXaccess.common.model.QualificationDeleted(
                    adminActor.id,
                    fixedInstant,
                    correlationId,
                    qualificationId
                )
            )
        }

    @Test
    fun `given qualification cannot be found when deleting qualification then returns error`() = runTest {
        // given
        val error = Error.QualificationNotFound("message", qualificationId)

        whenever(qualificationRepository.getById(qualificationId))
            .thenReturn(error.left())

        // when
        val result = testee.deleteQualification(
            adminActor,
            correlationId,
            qualificationId
        )

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(error)
    }

    @Test
    fun `given sourcing event cannot be stored when deleting qualification then returns error`() = runTest {
        // given
        val qualification = QualificationFixture.arbitrary(qualificationId, aggregateVersion = 123)

        val event = QualificationDeleted(
            qualificationId,
            124,
            adminActor.id,
            fixedInstant,
            correlationId
        )

        val error = ErrorFixture.arbitrary()

        whenever(qualificationRepository.getById(qualificationId))
            .thenReturn(qualification.right())

        whenever(gettingToolsByQualificationId.getToolsByQualificationId(qualificationId))
            .thenReturn(setOf())

        whenever(qualificationRepository.store(event))
            .thenReturn(error.some())

        // when
        val result = testee.deleteQualification(
            adminActor,
            correlationId,
            qualificationId
        )

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(error)
    }

    @Test
    fun `given qualification is in use for tool when deleting qualification then returns error()`() = runTest {
        val qualification = QualificationFixture.arbitrary(qualificationId, aggregateVersion = 123)

        val toolId = ToolIdFixture.arbitrary()
        val tool = ToolFixture.arbitrary(toolId, requiredQualifications = setOf(qualificationId))

        whenever(qualificationRepository.getById(qualificationId))
            .thenReturn(qualification.right())

        whenever(gettingToolsByQualificationId.getToolsByQualificationId(qualificationId))
            .thenReturn(setOf(tool))

        // when
        val result = testee.deleteQualification(
            adminActor,
            correlationId,
            qualificationId
        )

        val expectedError = Error.QualificationInUse(
            "Qualification in use by tools ($toolId).",
            qualificationId,
            setOf(toolId),
            correlationId
        )

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(expectedError)
    }
}