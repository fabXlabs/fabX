package cloud.fabX.fabXaccess.qualification.application

import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isSameAs
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.ErrorFixture
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.qualification.model.QualificationFixture
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFixture
import cloud.fabX.fabXaccess.qualification.model.QualificationRepository
import cloud.fabX.fabXaccess.user.model.InstructorFixture
import isLeft
import isRight
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.whenever

@MockitoSettings
internal class GettingQualificationTest {

    private val actor = InstructorFixture.arbitrary()
    private val qualificationId = QualificationIdFixture.arbitrary()
    private val correlationId = CorrelationIdFixture.arbitrary()

    private var logger: Logger? = null
    private var qualificationRepository: QualificationRepository? = null

    private var testee: GettingQualification? = null

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock qualificationRepository: QualificationRepository
    ) {
        this.logger = logger
        this.qualificationRepository = qualificationRepository

        testee = GettingQualification({ logger }, qualificationRepository)
    }

    @Test
    fun `when getting all then returns all from repository`() {
        // given
        val qualification1 = QualificationFixture.arbitrary()
        val qualification2 = QualificationFixture.arbitrary()

        val qualifications = setOf(qualification1, qualification2)

        whenever(qualificationRepository!!.getAll())
            .thenReturn(qualifications)

        // when
        val result = testee!!.getAll(actor, correlationId)

        // then
        assertThat(result)
            .isSameAs(qualifications)
    }

    @Test
    fun `given qualification exists when getting by id then returns from repository`() {
        // given
        val qualification = QualificationFixture.arbitrary(qualificationId)

        whenever(qualificationRepository!!.getById(qualificationId))
            .thenReturn(qualification.right())

        // when
        val result = testee!!.getById(actor, correlationId, qualificationId)

        // then
        assertThat(result)
            .isRight()
            .isSameAs(qualification)
    }

    @Test
    fun `given repository error when getting by id then returns error`() {
        // given
        val expectedError = ErrorFixture.arbitrary()

        whenever(qualificationRepository!!.getById(qualificationId))
            .thenReturn(expectedError.left())

        // when
        val result = testee!!.getById(actor, correlationId, qualificationId)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(expectedError)
    }
}