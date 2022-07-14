package cloud.fabX.fabXaccess.user.model

import arrow.core.getOrElse

object AdminFixture {
    fun arbitrary(
        userId: UserId = UserIdFixture.arbitrary(),
    ): Admin =
        UserFixture.arbitrary(
            userId = userId,
            isAdmin = true
        )
            .asAdmin()
            .getOrElse { throw Exception("Not able to get user as admin") }
}