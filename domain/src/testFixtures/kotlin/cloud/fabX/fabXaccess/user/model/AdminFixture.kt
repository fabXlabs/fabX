package cloud.fabX.fabXaccess.user.model

import arrow.core.getOrElse
import cloud.fabX.fabXaccess.common.model.UserId

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