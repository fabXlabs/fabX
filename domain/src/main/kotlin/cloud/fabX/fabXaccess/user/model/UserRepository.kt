package cloud.fabX.fabXaccess.user.model

interface UserRepository {
    fun getById(id: UserId): User?
    fun store(event: UserSourcingEvent)
}