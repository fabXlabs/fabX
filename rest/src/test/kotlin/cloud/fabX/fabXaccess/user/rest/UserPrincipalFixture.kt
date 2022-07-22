package cloud.fabX.fabXaccess.user.rest

import cloud.fabX.fabXaccess.common.model.QualificationId
import cloud.fabX.fabXaccess.user.model.UserFixture

object UserPrincipalFixture {
    fun admin(): UserPrincipal {
        return UserPrincipal(UserFixture.arbitrary(isAdmin = true))
    }

    fun instructor(qualificationId: QualificationId): UserPrincipal {
        return UserPrincipal(UserFixture.arbitrary(instructorQualifications = setOf(qualificationId)))
    }

    fun member(): UserPrincipal {
        return UserPrincipal(UserFixture.arbitrary())
    }
}