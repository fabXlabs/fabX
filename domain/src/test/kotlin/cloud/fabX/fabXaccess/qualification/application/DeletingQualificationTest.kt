package cloud.fabX.fabXaccess.qualification.application

import arrow.core.None
import arrow.core.left
import arrow.core.right
import arrow.core.some
import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.ErrorFixture
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.qualification.model.QualificationDeleted
import cloud.fabX.fabXaccess.qualification.model.QualificationFixture
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFixture
import cloud.fabX.fabXaccess.qualification.model.QualificationRepository
import cloud.fabX.fabXaccess.user.model.AdminFixture
import isNone
import isSome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.whenever

@MockitoSettings
internal class DeletingQualificationTest {

    private val adminActor = AdminFixture.arbitraryAdmin()

    private val qualificationId = QualificationIdFixture.arbitrary()

    private var logger: Logger? = null
    private var qualificationRepository: QualificationRepository? = null

    private var testee: DeletingQualification? = null

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock qualificationRepository: QualificationRepository
    ) {
        this.logger = logger
        this.qualificationRepository = qualificationRepository
        DomainModule.configureLoggerFactory { logger }
        DomainModule.configureQualificationRepository(qualificationRepository)

        testee = DeletingQualification()
    }

    @Test
    fun `given qualification can be found when deleting qualification then sourcing event is created and stored`() {
        // given
        val qualification = QualificationFixture.arbitrary(qualificationId, aggregateVersion = 123)

        val expectedSourcingEvent = QualificationDeleted(
            qualificationId,
            124,
            adminActor.id
        )

        whenever(qualificationRepository!!.getById(qualificationId))
            .thenReturn(qualification.right())

        whenever(qualificationRepository!!.store(expectedSourcingEvent))
            .thenReturn(None)

        // when
        val result = testee!!.deleteQualification(
            adminActor,
            qualificationId
        )

        // then
        assertThat(result).isNone()
    }

    @Test
    fun `given qualification cannot be found when deleting qualification then returns error`() {
        // given
        val error = Error.QualificationNotFound("message", qualificationId)

        whenever(qualificationRepository!!.getById(qualificationId))
            .thenReturn(error.left())

        // when
        val result = testee!!.deleteQualification(
            adminActor,
            qualificationId
        )

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(error)
    }

    @Test
    fun `given sourcing event cannot be stored when deleting qualification then returns error`() {
        // given
        val qualification = QualificationFixture.arbitrary(qualificationId, aggregateVersion = 123)

        val event = QualificationDeleted(
            qualificationId,
            124,
            adminActor.id
        )

        val error = ErrorFixture.arbitrary()

        whenever(qualificationRepository!!.getById(qualificationId))
            .thenReturn(qualification.right())

        whenever(qualificationRepository!!.store(event))
            .thenReturn(error.some())

        // when
        val result = testee!!.deleteQualification(
            adminActor,
            qualificationId
        )

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(error)
    }
}