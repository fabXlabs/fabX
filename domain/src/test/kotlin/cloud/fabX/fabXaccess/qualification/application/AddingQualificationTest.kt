package cloud.fabX.fabXaccess.qualification.application

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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.whenever

@MockitoSettings
internal class AddingQualificationTest {

    private val adminActor = AdminFixture.arbitrary()
    private val correlationId = CorrelationIdFixture.arbitrary()

    private val qualificationId = QualificationIdFixture.arbitrary()

    private var logger: Logger? = null
    private var qualificationRepository: QualificationRepository? = null

    private var testee: AddingQualification? = null

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock qualificationRepository: QualificationRepository
    ) {
        this.logger = logger
        this.qualificationRepository = qualificationRepository

        DomainModule.configureLoggerFactory { logger }
        DomainModule.configureQualificationIdFactory { qualificationId }
        DomainModule.configureQualificationRepository(qualificationRepository)

        testee = AddingQualification()
    }

    @Test
    fun `given valid values when adding qualification then sourcing event is created and stored`() {
        // given
        val name = "name"
        val description = "description"
        val colour = "#654321"
        val orderNr = 678

        val expectedSourcingEvent = QualificationCreated(
            qualificationId,
            adminActor.id,
            correlationId,
            name,
            description,
            colour,
            orderNr
        )

        whenever(qualificationRepository!!.store(expectedSourcingEvent))
            .thenReturn(None)

        // when
        val result = testee!!.addQualification(
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
    fun `given sourcing event cannot be stored when adding qualification then returns error`() {
        // given
        val name = "name"
        val description = "description"
        val colour = "#654321"
        val orderNr = 678

        val event = QualificationCreated(
            qualificationId,
            adminActor.id,
            correlationId,
            name,
            description,
            colour,
            orderNr
        )

        val error = ErrorFixture.arbitrary()

        whenever(qualificationRepository!!.store(event))
            .thenReturn(error.some())

        // when
        val result = testee!!.addQualification(
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