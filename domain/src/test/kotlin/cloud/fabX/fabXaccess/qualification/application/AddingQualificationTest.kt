package cloud.fabX.fabXaccess.qualification.application

import FixedClock
import arrow.core.None
import arrow.core.some
import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.ErrorFixture
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.qualification.model.QualificationCreated
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFixture
import cloud.fabX.fabXaccess.qualification.model.QualificationRepository
import cloud.fabX.fabXaccess.user.model.AdminFixture
import isLeft
import isRight
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@MockitoSettings
internal class AddingQualificationTest {

    private val adminActor = AdminFixture.arbitrary()
    private val correlationId = CorrelationIdFixture.arbitrary()

    private val qualificationId = QualificationIdFixture.arbitrary()

    private val fixedInstant = Clock.System.now()
    private val fixedClock = FixedClock(fixedInstant)

    private lateinit var logger: Logger
    private lateinit var qualificationRepository: QualificationRepository

    private lateinit var testee: AddingQualification

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock qualificationRepository: QualificationRepository
    ) {
        this.logger = logger
        this.qualificationRepository = qualificationRepository

        testee = AddingQualification({ logger }, qualificationRepository, { qualificationId }, fixedClock)
    }

    @Test
    fun `given valid values when adding qualification then sourcing event is created and stored`() = runTest {
        // given
        val name = "name"
        val description = "description"
        val colour = "#654321"
        val orderNr = 678

        val expectedSourcingEvent = QualificationCreated(
            qualificationId,
            adminActor.id,
            fixedInstant,
            correlationId,
            name,
            description,
            colour,
            orderNr
        )

        whenever(qualificationRepository.store(expectedSourcingEvent))
            .thenReturn(None)

        // when
        val result = testee.addQualification(
            adminActor,
            correlationId,
            name,
            description,
            colour,
            orderNr
        )

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(qualificationId)
    }

    @Test
    fun `given sourcing event cannot be stored when adding qualification then returns error`() = runTest {
        // given
        val name = "name"
        val description = "description"
        val colour = "#654321"
        val orderNr = 678

        val event = QualificationCreated(
            qualificationId,
            adminActor.id,
            fixedInstant,
            correlationId,
            name,
            description,
            colour,
            orderNr
        )

        val error = ErrorFixture.arbitrary()

        whenever(qualificationRepository.store(event))
            .thenReturn(error.some())

        // when
        val result = testee.addQualification(
            adminActor,
            correlationId,
            name,
            description,
            colour,
            orderNr
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }
}