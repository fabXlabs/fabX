package cloud.fabX.fabXaccess.user.model

import cloud.fabX.fabXaccess.qualification.model.QualificationId
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFixture

object UserFixture {

    fun arbitraryUser(
        userId: UserId = UserIdFixture.arbitraryId(),
        firstName: String = "first",
        lastName: String = "last",
        wikiName: String = "wiki",
        phoneNumber: String? = null,
        locked: Boolean = false,
        notes: String? = null,
        memberQualifications: List<QualificationId> = listOf(QualificationIdFixture.arbitraryId()),
        instructorQualifications: List<QualificationId>? = null,
        isAdmin: Boolean = false
    ): User = User(
        userId,
        firstName,
        lastName,
        wikiName,
        phoneNumber,
        locked,
        notes,
        Member(memberQualifications),
        if (instructorQualifications != null) Instructor(instructorQualifications) else null,
        if (isAdmin) Admin() else null
    )
}