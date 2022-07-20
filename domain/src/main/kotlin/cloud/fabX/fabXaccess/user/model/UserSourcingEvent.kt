package cloud.fabX.fabXaccess.user.model

import arrow.core.Option
import cloud.fabX.fabXaccess.common.model.ActorId
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.QualificationId
import cloud.fabX.fabXaccess.common.model.SourcingEvent
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.common.model.newUserId
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

sealed class UserSourcingEvent(
    override val aggregateRootId: UserId,
    override val aggregateVersion: Long,
    override val actorId: ActorId,
    override val correlationId: CorrelationId,
    override val timestamp: Instant = Clock.System.now()
) : SourcingEvent {

    abstract fun processBy(eventHandler: EventHandler, user: Option<User>): Option<User>

    interface EventHandler {
        fun handle(event: UserCreated, user: Option<User>): Option<User>
        fun handle(event: UserPersonalInformationChanged, user: Option<User>): Option<User>
        fun handle(event: UserLockStateChanged, user: Option<User>): Option<User>

        fun handle(event: UsernamePasswordIdentityAdded, user: Option<User>): Option<User>
        fun handle(event: UsernamePasswordIdentityRemoved, user: Option<User>): Option<User>
        fun handle(event: CardIdentityAdded, user: Option<User>): Option<User>
        fun handle(event: CardIdentityRemoved, user: Option<User>): Option<User>
        fun handle(event: PhoneNrIdentityAdded, user: Option<User>): Option<User>
        fun handle(event: PhoneNrIdentityRemoved, user: Option<User>): Option<User>

        fun handle(event: MemberQualificationAdded, user: Option<User>): Option<User>
        fun handle(event: MemberQualificationRemoved, user: Option<User>): Option<User>

        fun handle(event: InstructorQualificationAdded, user: Option<User>): Option<User>
        fun handle(event: InstructorQualificationRemoved, user: Option<User>): Option<User>

        fun handle(event: IsAdminChanged, user: Option<User>): Option<User>

        fun handle(event: UserDeleted, user: Option<User>): Option<User>
    }
}

data class UserCreated(
    override val aggregateRootId: UserId = newUserId(),
    override val actorId: ActorId,
    override val correlationId: CorrelationId,
    val firstName: String,
    val lastName: String,
    val wikiName: String
) : UserSourcingEvent(aggregateRootId, 1, actorId, correlationId) {

    override fun processBy(eventHandler: EventHandler, user: Option<User>): Option<User> =
        eventHandler.handle(this, user)
}

data class UserPersonalInformationChanged(
    override val aggregateRootId: UserId,
    override val aggregateVersion: Long,
    override val actorId: ActorId,
    override val correlationId: CorrelationId,
    val firstName: ChangeableValue<String>,
    val lastName: ChangeableValue<String>,
    val wikiName: ChangeableValue<String>
) : UserSourcingEvent(aggregateRootId, aggregateVersion, actorId, correlationId) {

    override fun processBy(eventHandler: EventHandler, user: Option<User>): Option<User> =
        eventHandler.handle(this, user)
}

data class UserLockStateChanged(
    override val aggregateRootId: UserId,
    override val aggregateVersion: Long,
    override val actorId: ActorId,
    override val correlationId: CorrelationId,
    val locked: ChangeableValue<Boolean>,
    val notes: ChangeableValue<String?>
) : UserSourcingEvent(aggregateRootId, aggregateVersion, actorId, correlationId) {

    override fun processBy(eventHandler: EventHandler, user: Option<User>): Option<User> =
        eventHandler.handle(this, user)
}

data class UsernamePasswordIdentityAdded(
    override val aggregateRootId: UserId,
    override val aggregateVersion: Long,
    override val actorId: ActorId,
    override val correlationId: CorrelationId,
    val username: String,
    val hash: String
) : UserSourcingEvent(aggregateRootId, aggregateVersion, actorId, correlationId) {

    override fun processBy(eventHandler: EventHandler, user: Option<User>): Option<User> =
        eventHandler.handle(this, user)
}

data class UsernamePasswordIdentityRemoved(
    override val aggregateRootId: UserId,
    override val aggregateVersion: Long,
    override val actorId: ActorId,
    override val correlationId: CorrelationId,
    val username: String
) : UserSourcingEvent(aggregateRootId, aggregateVersion, actorId, correlationId) {

    override fun processBy(eventHandler: EventHandler, user: Option<User>): Option<User> =
        eventHandler.handle(this, user)
}

data class CardIdentityAdded(
    override val aggregateRootId: UserId,
    override val aggregateVersion: Long,
    override val actorId: ActorId,
    override val correlationId: CorrelationId,
    val cardId: String,
    val cardSecret: String
) : UserSourcingEvent(aggregateRootId, aggregateVersion, actorId, correlationId) {

    override fun processBy(eventHandler: EventHandler, user: Option<User>): Option<User> =
        eventHandler.handle(this, user)
}

data class CardIdentityRemoved(
    override val aggregateRootId: UserId,
    override val aggregateVersion: Long,
    override val actorId: ActorId,
    override val correlationId: CorrelationId,
    val cardId: String
) : UserSourcingEvent(aggregateRootId, aggregateVersion, actorId, correlationId) {

    override fun processBy(eventHandler: EventHandler, user: Option<User>): Option<User> =
        eventHandler.handle(this, user)
}

data class PhoneNrIdentityAdded(
    override val aggregateRootId: UserId,
    override val aggregateVersion: Long,
    override val actorId: ActorId,
    override val correlationId: CorrelationId,
    val phoneNr: String
) : UserSourcingEvent(aggregateRootId, aggregateVersion, actorId, correlationId) {

    override fun processBy(eventHandler: EventHandler, user: Option<User>): Option<User> =
        eventHandler.handle(this, user)
}

data class PhoneNrIdentityRemoved(
    override val aggregateRootId: UserId,
    override val aggregateVersion: Long,
    override val actorId: ActorId,
    override val correlationId: CorrelationId,
    val phoneNr: String
) : UserSourcingEvent(aggregateRootId, aggregateVersion, actorId, correlationId) {

    override fun processBy(eventHandler: EventHandler, user: Option<User>): Option<User> =
        eventHandler.handle(this, user)
}

data class MemberQualificationAdded(
    override val aggregateRootId: UserId,
    override val aggregateVersion: Long,
    override val actorId: ActorId,
    override val correlationId: CorrelationId,
    val qualificationId: QualificationId
) : UserSourcingEvent(aggregateRootId, aggregateVersion, actorId, correlationId) {

    override fun processBy(eventHandler: EventHandler, user: Option<User>): Option<User> =
        eventHandler.handle(this, user)
}

data class MemberQualificationRemoved(
    override val aggregateRootId: UserId,
    override val aggregateVersion: Long,
    override val actorId: ActorId,
    override val correlationId: CorrelationId,
    val qualificationId: QualificationId
) : UserSourcingEvent(aggregateRootId, aggregateVersion, actorId, correlationId) {

    override fun processBy(eventHandler: EventHandler, user: Option<User>): Option<User> =
        eventHandler.handle(this, user)
}

data class InstructorQualificationAdded(
    override val aggregateRootId: UserId,
    override val aggregateVersion: Long,
    override val actorId: ActorId,
    override val correlationId: CorrelationId,
    val qualificationId: QualificationId
) : UserSourcingEvent(aggregateRootId, aggregateVersion, actorId, correlationId) {

    override fun processBy(eventHandler: EventHandler, user: Option<User>): Option<User> =
        eventHandler.handle(this, user)
}

data class InstructorQualificationRemoved(
    override val aggregateRootId: UserId,
    override val aggregateVersion: Long,
    override val actorId: ActorId,
    override val correlationId: CorrelationId,
    val qualificationId: QualificationId
) : UserSourcingEvent(aggregateRootId, aggregateVersion, actorId, correlationId) {

    override fun processBy(eventHandler: EventHandler, user: Option<User>): Option<User> =
        eventHandler.handle(this, user)
}

data class IsAdminChanged(
    override val aggregateRootId: UserId,
    override val aggregateVersion: Long,
    override val actorId: ActorId,
    override val correlationId: CorrelationId,
    val isAdmin: Boolean
) : UserSourcingEvent(aggregateRootId, aggregateVersion, actorId, correlationId) {

    override fun processBy(eventHandler: EventHandler, user: Option<User>): Option<User> =
        eventHandler.handle(this, user)
}

data class UserDeleted(
    override val aggregateRootId: UserId,
    override val aggregateVersion: Long,
    override val actorId: ActorId,
    override val correlationId: CorrelationId,
) : UserSourcingEvent(aggregateRootId, aggregateVersion, actorId, correlationId) {

    override fun processBy(eventHandler: EventHandler, user: Option<User>): Option<User> =
        eventHandler.handle(this, user)
}
