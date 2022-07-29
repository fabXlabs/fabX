package cloud.fabX.fabXaccess.qualification.application

import FixedClock
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
import cloud.fabX.fabXaccess.qualification.model.QualificationDetailsChanged
import cloud.fabX.fabXaccess.qualification.model.QualificationFixture
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFixture
import cloud.fabX.fabXaccess.qualification.model.QualificationRepository
import cloud.fabX.fabXaccess.user.model.AdminFixture
import isNone
import isSome
import kotlinx.datetime.Clock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.whenever

@MockitoSettings
internal class ChangingQualificationTest {

    private val adminActor = AdminFixture.arbitrary()
    private val correlationId = CorrelationIdFixture.arbitrary()

    private val qualificationId = QualificationIdFixture.arbitrary()

    private val fixedInstant = Clock.System.now()
    private val fixedClock = FixedClock(fixedInstant)

    private lateinit var logger: Logger
    private lateinit var qualificationRepository: QualificationRepository

    private lateinit var testee: ChangingQualification

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock qualificationRepository: QualificationRepository
    ) {
        this.logger = logger
        this.qualificationRepository = qualificationRepository

        testee = ChangingQualification({ logger }, qualificationRepository, fixedClock)
    }

    @Test
    fun `given qualification can be found when changing qualification details then sourcing event is created and stored`() {
        // given
        val qualification = QualificationFixture.arbitrary(qualificationId, aggregateVersion = 1)

        val newName = ChangeableValue.ChangeToValueString("newName")
        val newDescription = ChangeableValue.ChangeToValueString("newDescription")
        val newColour = ChangeableValue.ChangeToValueString("#424242")
        val newOrderNr = ChangeableValue.LeaveAsIs

        val expectedSourcingEvent = QualificationDetailsChanged(
            qualificationId,
            2,
            adminActor.id,
            fixedInstant,
            correlationId,
            newName,
            newDescription,
            newColour,
            newOrderNr
        )

        whenever(qualificationRepository.getById(qualificationId))
            .thenReturn(qualification.right())

        whenever(qualificationRepository.store(eq(expectedSourcingEvent)))
            .thenReturn(None)

        // when
        val result = testee.changeQualificationDetails(
            adminActor,
            correlationId,
            qualificationId,
            newName,
            newDescription,
            newColour,
            newOrderNr
        )

        // then
        assertThat(result).isNone()

        val inOrder = inOrder(logger, qualificationRepository)
        inOrder.verify(logger).debug("changeQualificationDetails...")
        inOrder.verify(qualificationRepository).getById(qualificationId)
        inOrder.verify(qualificationRepository).store(expectedSourcingEvent)
        inOrder.verify(logger).debug("...changeQualificationDetails done")
    }

    @Test
    fun `given qualification cannot be found when changing qualification details then returns error`() {
        // given
        val error = Error.QualificationNotFound("message", qualificationId)

        whenever(qualificationRepository.getById(qualificationId))
            .thenReturn(error.left())

        // when
        val result = testee.changeQualificationDetails(
            adminActor,
            correlationId,
            qualificationId,
            ChangeableValue.LeaveAsIs,
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
    fun `given sourcing event cannot be stored when changing qualification details then returns error`() {
        // given
        val qualification = QualificationFixture.arbitrary(qualificationId, aggregateVersion = 1)

        val event = QualificationDetailsChanged(
            qualificationId,
            2,
            adminActor.id,
            fixedInstant,
            correlationId,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs
        )

        val error = ErrorFixture.arbitrary()

        whenever(qualificationRepository.getById(qualificationId))
            .thenReturn(qualification.right())

        whenever(qualificationRepository.store(event))
            .thenReturn(error.some())

        // when
        val result = testee.changeQualificationDetails(
            adminActor,
            correlationId,
            qualificationId,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs
        )

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(error)
    }
}