package cloud.fabX.fabXaccess.user.model

import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.SourcingEvent

sealed class UserSourcingEvent(
    override val aggregateRootId: UserId,
    override val aggregateVersion: Long
) : SourcingEvent {

    abstract fun processBy(eventHandler: EventHandler, user: User): User

    interface EventHandler {
        fun handle(event: UserPersonalInformationChanged, user: User): User
        fun handle(event: UserLockStateChanged, user: User): User
    }
}

// TODO UserCreated sourcing event

data class UserPersonalInformationChanged(
    override val aggregateRootId: UserId,
    override val aggregateVersion: Long,
    val firstName: ChangeableValue<String>,
    val lastName: ChangeableValue<String>,
    val wikiName: ChangeableValue<String>,
    val phoneNumber: ChangeableValue<String?>
) : UserSourcingEvent(aggregateRootId, aggregateVersion) {

    override fun processBy(eventHandler: EventHandler, user: User): User =
        eventHandler.handle(this, user)
}

data class UserLockStateChanged(
    override val aggregateRootId: UserId,
    override val aggregateVersion: Long,
    val locked: ChangeableValue<Boolean>,
    val notes: ChangeableValue<String?>
) : UserSourcingEvent(aggregateRootId, aggregateVersion) {

    override fun processBy(eventHandler: EventHandler, user: User): User =
        eventHandler.handle(this, user)
}