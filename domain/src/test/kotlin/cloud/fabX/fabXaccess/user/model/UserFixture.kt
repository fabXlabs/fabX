package cloud.fabX.fabXaccess.user.model

object UserFixture {

    fun arbitraryUser(
        userId: UserId = UserIdFixture.arbitraryId()
    ): User = User(
        userId,
        "first",
        "last",
        "wiki",
        null,
        false,
        null
    )
}