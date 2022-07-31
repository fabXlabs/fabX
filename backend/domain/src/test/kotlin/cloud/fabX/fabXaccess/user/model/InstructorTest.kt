package cloud.fabX.fabXaccess.user.model

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFixture
import org.junit.jupiter.api.Test

internal class InstructorTest {
    @Test
    fun `given instructor has qualification when checking has qualification then returns true`() {
        // given
        val qualificationId = QualificationIdFixture.arbitrary()

        val instructor = Instructor(UserIdFixture.arbitrary(), "name", setOf(qualificationId))

        // when
        val result = instructor.hasQualification(qualificationId)

        // then
        assertThat(result).isTrue()
    }

    @Test
    fun `given instructor not has qualification when checking has qualification then returns false`() {
        // given
        val qualificationId = QualificationIdFixture.static(1)
        val otherQualificationId = QualificationIdFixture.static(2)

        val instructor = Instructor(UserIdFixture.arbitrary(), "name", setOf(qualificationId))

        // when
        val result = instructor.hasQualification(otherQualificationId)

        // then
        assertThat(result).isFalse()
    }
}