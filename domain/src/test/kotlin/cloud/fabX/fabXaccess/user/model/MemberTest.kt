package cloud.fabX.fabXaccess.user.model

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFixture
import org.junit.jupiter.api.Test

internal class MemberTest {

    @Test
    fun `given member has qualification when checking has qualification then returns true`() {
        // given
        val qualificationId = QualificationIdFixture.arbitrary()

        val member = Member(UserIdFixture.arbitrary(), "name", setOf(qualificationId))

        // when
        val result = member.hasQualification(qualificationId)

        // then
        assertThat(result).isTrue()
    }

    @Test
    fun `given member not has qualification when checking has qualification then returns false`() {
        // given
        val qualificationId = QualificationIdFixture.static(1)
        val otherQualificationId = QualificationIdFixture.static(2)

        val member = Member(UserIdFixture.arbitrary(), "name", setOf(qualificationId))

        // when
        val result = member.hasQualification(otherQualificationId)

        // then
        assertThat(result).isFalse()
    }
}