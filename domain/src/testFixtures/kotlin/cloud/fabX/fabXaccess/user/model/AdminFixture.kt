package cloud.fabX.fabXaccess.user.model

import arrow.core.getOrElse

object AdminFixture {
    fun arbitraryAdmin(
        userId: UserId = UserIdFixture.arbitraryId(),
    ): Admin =
        UserFixture.arbitrary(
            userId = userId,
            isAdmin = true
        )
            .asAdmin()
            .getOrElse { throw Exception("Not able to get user as admin") }
}