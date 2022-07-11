package cloud.fabX.fabXaccess.user.model

import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.SourcingEvent

sealed class UserSourcingEvent(override val aggregateRootId: UserId) : SourcingEvent {
    abstract fun processBy(eventHandler: EventHandler, dao: MutableList<User>)

    interface EventHandler {
        fun handle(event: UserValuesChanged, dao: MutableList<User>)
    }
}

data class UserValuesChanged(
    override val aggregateRootId: UserId,
    val firstName: ChangeableValue<String>
) : UserSourcingEvent(aggregateRootId) {
    override fun processBy(eventHandler: EventHandler, dao: MutableList<User>) {
        eventHandler.handle(this, dao)
    }
}
