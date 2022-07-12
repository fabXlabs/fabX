package cloud.fabX.fabXaccess.user.model

import cloud.fabX.fabXaccess.common.model.ActorId
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.SourcingEvent
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

sealed class UserSourcingEvent(
    override val aggregateRootId: UserId,
    override val aggregateVersion: Long,
    override val actorId: ActorId,
    override val timestamp: Instant = Clock.System.now()
) : SourcingEvent {

    abstract fun processBy(eventHandler: EventHandler, user: User): User

    interface EventHandler {
        fun handle(event: UserCreated, user: User): User
        fun handle(event: UserPersonalInformationChanged, user: User): User
        fun handle(event: UserLockStateChanged, user: User): User
    }
}

data class UserCreated(
    override val aggregateRootId: UserId = newUserId(),
    override val actorId: ActorId,
    val firstName: String,
    val lastName: String,
    val wikiName: String,
    val phoneNumber: String?
) : UserSourcingEvent(aggregateRootId, 1, actorId) {

    override fun processBy(eventHandler: EventHandler, user: User): User =
        eventHandler.handle(this, user)

}

data class UserPersonalInformationChanged(
    override val aggregateRootId: UserId,
    override val aggregateVersion: Long,
    override val actorId: ActorId,
    val firstName: ChangeableValue<String>,
    val lastName: ChangeableValue<String>,
    val wikiName: ChangeableValue<String>,
    val phoneNumber: ChangeableValue<String?>
) : UserSourcingEvent(aggregateRootId, aggregateVersion, actorId) {

    override fun processBy(eventHandler: EventHandler, user: User): User =
        eventHandler.handle(this, user)
}

data class UserLockStateChanged(
    override val aggregateRootId: UserId,
    override val aggregateVersion: Long,
    override val actorId: ActorId,
    val locked: ChangeableValue<Boolean>,
    val notes: ChangeableValue<String?>
) : UserSourcingEvent(aggregateRootId, aggregateVersion, actorId) {

    override fun processBy(eventHandler: EventHandler, user: User): User =
        eventHandler.handle(this, user)
}