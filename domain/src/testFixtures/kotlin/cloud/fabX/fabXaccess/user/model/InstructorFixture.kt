package cloud.fabX.fabXaccess.user.model

import arrow.core.getOrElse
import cloud.fabX.fabXaccess.common.model.QualificationId
import cloud.fabX.fabXaccess.common.model.UserId

object InstructorFixture {
    fun arbitrary(
        userId: UserId = UserIdFixture.arbitrary(),
        qualifications: Set<QualificationId> = setOf()
    ): Instructor {
        return UserFixture.arbitrary(
            userId = userId,
            instructorQualifications = qualifications
        )
            .asInstructor()
            .getOrElse { throw Exception("Not able to get user as instructor") }
    }
}