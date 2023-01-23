package cloud.fabX.fabXaccess.user.model

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.getOrElse
import cloud.fabX.fabXaccess.common.model.valueToChangeTo
import java.util.stream.Collectors

internal class UserEventHandler : UserSourcingEvent.EventHandler {

    override fun handle(event: UserCreated, user: Option<User>): Option<User> {
        if (user.isDefined()) {
            throw AccumulatorNotEmptyForUserCreatedEventHandler(
                "Handler for UserCreated is given $user."
            )
        }

        return Some(
            User(
                id = event.aggregateRootId,
                aggregateVersion = event.aggregateVersion,
                firstName = event.firstName,
                lastName = event.lastName,
                wikiName = event.wikiName,
                locked = false,
                notes = null,
                identities = setOf(),
                memberQualifications = setOf(),
                instructorQualifications = null,
                isAdmin = false
            )
        )
    }

    override fun handle(
        event: UserPersonalInformationChanged,
        user: Option<User>
    ): Option<User> =
        requireSomeUserWithSameIdAndSome(event, user) { e, u ->
            u.copy(
                aggregateVersion = e.aggregateVersion,
                firstName = e.firstName.valueToChangeTo(u.firstName),
                lastName = e.lastName.valueToChangeTo(u.lastName),
                wikiName = e.wikiName.valueToChangeTo(u.wikiName),
            )
        }

    override fun handle(event: UserLockStateChanged, user: Option<User>): Option<User> =
        requireSomeUserWithSameIdAndSome(event, user) { e, u ->
            u.copy(
                aggregateVersion = e.aggregateVersion,
                locked = e.locked.valueToChangeTo(u.locked),
                notes = e.notes.valueToChangeTo(u.notes)
            )
        }

    override fun handle(event: UsernamePasswordIdentityAdded, user: Option<User>): Option<User> =
        requireSomeUserWithSameIdAndSome(event, user) { e, u ->
            u.copy(
                aggregateVersion = e.aggregateVersion,
                identities = u.identities + UsernamePasswordIdentity(
                    e.username,
                    e.hash
                )
            )
        }

    override fun handle(event: PasswordChanged, user: Option<User>): Option<User> =
        requireSomeUserWithSameIdAndSome(event, user) { e, u ->
            u.copy(
                aggregateVersion = e.aggregateVersion,
                identities = u.identities.stream()
                    .map {
                        if (it is UsernamePasswordIdentity) {
                            it.copy(hash = e.hash)
                        } else {
                            it
                        }
                    }
                    .collect(Collectors.toSet())
            )
        }

    override fun handle(event: UsernamePasswordIdentityRemoved, user: Option<User>): Option<User> =
        requireSomeUserWithSameIdAndSome(event, user) { e, u ->
            u.copy(
                aggregateVersion = e.aggregateVersion,
                identities = u.identities.stream()
                    .filter { !(it is UsernamePasswordIdentity && it.username == e.username) }
                    .collect(Collectors.toSet())
            )
        }

    override fun handle(event: WebauthnIdentityAdded, user: Option<User>): Option<User> =
        requireSomeUserWithSameIdAndSome(event, user) { e, u ->
            u.copy(
                aggregateVersion = e.aggregateVersion,
                identities = u.identities + WebauthnIdentity(
                    authenticator = e.authenticator
                )
            )
        }

    override fun handle(event: WebauthnIdentityRemoved, user: Option<User>): Option<User> {
        return requireSomeUserWithSameIdAndSome(event, user) { e, u ->
            u.copy(
                aggregateVersion = e.aggregateVersion,
                identities = u.identities.stream()
                    .filter {
                        !(it is WebauthnIdentity
                            && it.authenticator.attestedCredentialData.credentialId.contentEquals(e.credentialId))
                    }
                    .collect(Collectors.toSet())
            )
        }
    }

    override fun handle(event: CardIdentityAdded, user: Option<User>): Option<User> =
        requireSomeUserWithSameIdAndSome(event, user) { e, u ->
            u.copy(
                aggregateVersion = e.aggregateVersion,
                identities = u.identities + CardIdentity(
                    e.cardId,
                    e.cardSecret
                )
            )
        }

    override fun handle(event: CardIdentityRemoved, user: Option<User>): Option<User> =
        requireSomeUserWithSameIdAndSome(event, user) { e, u ->
            u.copy(
                aggregateVersion = e.aggregateVersion,
                identities = u.identities.stream()
                    .filter { !(it is CardIdentity && it.cardId == e.cardId) }
                    .collect(Collectors.toSet())
            )
        }

    override fun handle(event: PhoneNrIdentityAdded, user: Option<User>): Option<User> =
        requireSomeUserWithSameIdAndSome(event, user) { e, u ->
            u.copy(
                aggregateVersion = e.aggregateVersion,
                identities = u.identities + PhoneNrIdentity(e.phoneNr)
            )
        }

    override fun handle(event: PhoneNrIdentityRemoved, user: Option<User>): Option<User> =
        requireSomeUserWithSameIdAndSome(event, user) { e, u ->
            u.copy(
                aggregateVersion = e.aggregateVersion,
                identities = u.identities.stream()
                    .filter { !(it is PhoneNrIdentity && it.phoneNr == e.phoneNr) }
                    .collect(Collectors.toSet())
            )
        }

    override fun handle(event: PinIdentityAdded, user: Option<User>): Option<User> =
        requireSomeUserWithSameIdAndSome(event, user) { e, u ->
            u.copy(
                aggregateVersion = e.aggregateVersion,
                identities = u.identities + PinIdentity(e.pin)
            )
        }

    override fun handle(event: PinIdentityRemoved, user: Option<User>): Option<User> =
        requireSomeUserWithSameIdAndSome(event, user) { e, u ->
            u.copy(
                aggregateVersion = e.aggregateVersion,
                identities = u.identities.stream()
                    .filter { it !is PinIdentity }
                    .collect(Collectors.toSet())
            )
        }

    override fun handle(event: MemberQualificationAdded, user: Option<User>): Option<User> =
        requireSomeUserWithSameIdAndSome(event, user) { e, u ->
            u.copy(
                aggregateVersion = e.aggregateVersion,
                memberQualifications = u.memberQualifications + e.qualificationId
            )
        }

    override fun handle(event: MemberQualificationRemoved, user: Option<User>): Option<User> =
        requireSomeUserWithSameIdAndSome(event, user) { e, u ->
            u.copy(
                aggregateVersion = e.aggregateVersion,
                memberQualifications = u.memberQualifications - e.qualificationId
            )
        }

    override fun handle(event: InstructorQualificationAdded, user: Option<User>): Option<User> =
        requireSomeUserWithSameIdAndSome(event, user) { e, u ->
            u.copy(
                aggregateVersion = e.aggregateVersion,
                instructorQualifications = u.instructorQualifications.orEmpty() + e.qualificationId
            )
        }

    override fun handle(event: InstructorQualificationRemoved, user: Option<User>): Option<User> {
        return requireSomeUserWithSameIdAndSome(event, user) { e, u ->
            val instructorQualifications = u.instructorQualifications.orEmpty() - e.qualificationId

            u.copy(
                aggregateVersion = e.aggregateVersion,
                instructorQualifications = instructorQualifications.ifEmpty { null }
            )
        }
    }

    override fun handle(event: IsAdminChanged, user: Option<User>): Option<User> {
        return requireSomeUserWithSameIdAndSome(event, user) { e, u ->
            u.copy(
                aggregateVersion = e.aggregateVersion,
                isAdmin = e.isAdmin
            )
        }
    }

    override fun handle(event: UserDeleted, user: Option<User>): Option<User> =
        requireSomeUserWithSameIdAnd(event, user) { _, _ ->
            None
        }

    private fun <E : UserSourcingEvent> requireSomeUserWithSameIdAnd(
        event: E,
        user: Option<User>,
        and: (E, User) -> Option<User>
    ): Option<User> {
        if (user.map { it.id != event.aggregateRootId }.getOrElse { false }) {
            throw EventAggregateRootIdDoesNotMatchUserId(
                "Event $event cannot be applied to $user. Aggregate root id does not match."
            )
        }

        return user.flatMap { and(event, it) }
    }

    private fun <E : UserSourcingEvent> requireSomeUserWithSameIdAndSome(
        event: E,
        user: Option<User>,
        and: (E, User) -> User
    ): Option<User> = requireSomeUserWithSameIdAnd(event, user) { e, u ->
        Some(and(e, u))
    }

    class EventAggregateRootIdDoesNotMatchUserId(message: String) : Exception(message)
    class AccumulatorNotEmptyForUserCreatedEventHandler(message: String) : Exception(message)
}