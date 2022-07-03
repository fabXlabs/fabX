package cloud.fabX.fabXaccess.user.model

import cloud.fabX.fabXaccess.common.model.SourcingEvent

class UserSourcingEvent(override val aggregateRootId: UserId) : SourcingEvent {
}

// TODO specific user sourcing events (e.g. for changing user attributes)
// TODO model tristate value change (new value, new value is null, leave value as is)