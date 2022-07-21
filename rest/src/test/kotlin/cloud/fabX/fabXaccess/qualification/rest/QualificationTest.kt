package cloud.fabX.fabXaccess.qualification.rest

import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.qualification.model.QualificationFixture
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFixture
import org.junit.jupiter.api.Test

internal class QualificationTest {
    @Test
    fun `when mapping to rest model then returns mapped`() {
        // given
        val qualificationId = QualificationIdFixture.arbitrary()
        val qualification = QualificationFixture.arbitrary(
            qualificationId,
            41,
            "qualification1",
            "description 1",
            "#111111",
            1
        )

        val expectedResult = Qualification(
            qualificationId.serialize(),
            41,
            "qualification1",
            "description 1",
            "#111111",
            1
        )

        // when
        val result = qualification.toRestModel()

        // then
        assertThat(result).isEqualTo(expectedResult)
    }
}