package cloud.fabX.fabXaccess.user.model

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import arrow.core.toOption
import cloud.fabX.fabXaccess.common.application.toHex
import cloud.fabX.fabXaccess.common.model.ActorId
import cloud.fabX.fabXaccess.common.model.AggregateRootEntity
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.DomainEvent
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.QualificationId
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.common.model.UserIdFactory
import cloud.fabX.fabXaccess.common.model.assertAggregateVersionIncreasesOneByOne
import cloud.fabX.fabXaccess.common.model.assertAggregateVersionStartsWithOne
import cloud.fabX.fabXaccess.common.model.assertIsNotEmpty
import cloud.fabX.fabXaccess.common.model.biFlatmap
import cloud.fabX.fabXaccess.qualification.model.GettingQualificationById
import com.webauthn4j.authenticator.Authenticator
import kotlinx.datetime.Clock

data class User internal constructor(
    override val id: UserId,
    override val aggregateVersion: Long,
    val firstName: String,
    val lastName: String,
    val wikiName: String,
    val locked: Boolean,
    val notes: String?,
    val identities: Set<UserIdentity>,
    val memberQualifications: Set<QualificationId>,
    val instructorQualifications: Set<QualificationId>?,
    val isAdmin: Boolean
) : AggregateRootEntity<UserId> {

    private val name: String
        get() = "$firstName $lastName"

    companion object {
        suspend fun addNew(
            userIdFactory: UserIdFactory,
            actor: Admin,
            clock: Clock,
            correlationId: CorrelationId,
            firstName: String,
            lastName: String,
            wikiName: String,
            gettingUserByWikiName: GettingUserByWikiName
        ): Either<Error, UserSourcingEvent> {
            return requireUniqueWikiName(wikiName, gettingUserByWikiName, correlationId)
                .map {
                    UserCreated(
                        userIdFactory.invoke(),
                        actor.id,
                        clock.now(),
                        correlationId,
                        firstName,
                        lastName,
                        wikiName
                    )
                }
        }

        fun fromSourcingEvents(events: Iterable<UserSourcingEvent>): Option<User> {
            events.assertIsNotEmpty()
            events.assertAggregateVersionStartsWithOne()
            events.assertAggregateVersionIncreasesOneByOne()

            val userCreatedEvent = events.first()

            if (userCreatedEvent !is UserCreated) {
                throw EventHistoryDoesNotStartWithUserCreated(
                    "Event history starts with ${userCreatedEvent}, not a UserCreated event."
                )
            }

            return events.fold(None) { result: Option<User>, event ->
                event.processBy(UserEventHandler(), result)
            }
        }

        private suspend fun requireUniqueWikiName(
            wikiName: String,
            gettingUserByWikiName: GettingUserByWikiName,
            correlationId: CorrelationId
        ): Either<Error, Unit> {
            return gettingUserByWikiName.getByWikiName(wikiName)
                .swap()
                .mapLeft {
                    Error.WikiNameAlreadyInUse(
                        "Wiki name is already in use.",
                        correlationId
                    )
                }
                .flatMap {
                    if (it is Error.UserNotFoundByWikiName) {
                        Unit.right()
                    } else {
                        it.left()
                    }
                }
        }
    }

    fun apply(sourcingEvent: UserSourcingEvent): Option<User> = sourcingEvent.processBy(UserEventHandler(), Some(this))

    suspend fun changePersonalInformation(
        actor: Admin,
        clock: Clock,
        correlationId: CorrelationId,
        firstName: ChangeableValue<String> = ChangeableValue.LeaveAsIs,
        lastName: ChangeableValue<String> = ChangeableValue.LeaveAsIs,
        wikiName: ChangeableValue<String> = ChangeableValue.LeaveAsIs,
        gettingUserByWikiName: GettingUserByWikiName
    ): Either<Error, UserSourcingEvent> {
        return wikiName.biFlatmap(
            { Unit.right() },
            { requireUniqueWikiName(it, gettingUserByWikiName, correlationId) }
        ).map {
            UserPersonalInformationChanged(
                id,
                aggregateVersion + 1,
                actor.id,
                clock.now(),
                correlationId,
                firstName,
                lastName,
                wikiName
            )
        }
    }

    fun changeLockState(
        actor: Admin,
        clock: Clock,
        correlationId: CorrelationId,
        locked: ChangeableValue<Boolean> = ChangeableValue.LeaveAsIs,
        notes: ChangeableValue<String?> = ChangeableValue.LeaveAsIs
    ): Either<Error, UserSourcingEvent> {
        return requireUserIsNotActor(actor, correlationId)
            .map {
                UserLockStateChanged(
                    id,
                    aggregateVersion + 1,
                    actor.id,
                    clock.now(),
                    correlationId,
                    locked,
                    notes
                )
            }
    }

    /**
     * Adds a username password identity to the user.
     *
     * Returns an error if the user already has a username password identity.
     * Returns an error if the username is already in use (by another user).
     *
     * @return error or sourcing event
     */
    suspend fun addUsernamePasswordIdentity(
        actor: Admin,
        clock: Clock,
        correlationId: CorrelationId,
        username: String,
        hash: String,
        gettingUserByUsername: GettingUserByUsername
    ): Either<Error, UserSourcingEvent> {
        return requireNoUsernamePasswordIdentity(correlationId)
            .flatMap { requireUniqueUsername(username, gettingUserByUsername, correlationId) }
            .flatMap { UsernamePasswordIdentity.fromUnvalidated(username, hash, correlationId) }
            .map {
                UsernamePasswordIdentityAdded(
                    id,
                    aggregateVersion + 1,
                    actor.id,
                    clock.now(),
                    correlationId,
                    it.username,
                    it.hash
                )
            }
    }

    private fun requireNoUsernamePasswordIdentity(
        correlationId: CorrelationId
    ): Either<Error, Unit> {
        return identities.firstOrNull { it is UsernamePasswordIdentity }
            .toOption()
            .map {
                Error.UsernamePasswordIdentityAlreadyFound(
                    "User already has a username password identity.",
                    correlationId
                )
            }
            .toEither { }
            .swap()
    }

    private suspend fun requireUniqueUsername(
        username: String,
        gettingUserByUsername: GettingUserByUsername,
        correlationId: CorrelationId
    ): Either<Error, Unit> {
        return gettingUserByUsername.getByUsername(username)
            .swap()
            .mapLeft {
                Error.UsernameAlreadyInUse(
                    "Username is already in use.",
                    correlationId
                )
            }
            .flatMap {
                if (it is Error.UserNotFoundByUsername) {
                    Unit.right()
                } else {
                    it.left()
                }
            }
    }

    /**
     * Changes the user's password.
     */
    fun changePassword(
        actor: Member,
        clock: Clock,
        correlationId: CorrelationId,
        hash: String
    ): Either<Error, UserSourcingEvent> {
        return requireUserIsActor(actor, correlationId)
            .flatMap { requireUsernamePasswordIdentity(correlationId) }
            .flatMap { UsernamePasswordIdentity.fromUnvalidated(it.username, hash, correlationId) }
            .map {
                PasswordChanged(
                    id,
                    aggregateVersion + 1,
                    actor.id,
                    clock.now(),
                    correlationId,
                    hash
                )
            }
    }

    private fun requireUsernamePasswordIdentity(
        correlationId: CorrelationId
    ): Either<Error, UsernamePasswordIdentity> {
        return identities.firstOrNull { it is UsernamePasswordIdentity }
            .toOption()
            .map { it as UsernamePasswordIdentity }
            .toEither {
                Error.UsernamePasswordIdentityNotFound(
                    "Not able to find username password identity.",
                    correlationId
                )
            }
    }

    /**
     * Removes the user's identity given by the username.
     *
     * @return error if no identity with given username exists, sourcing event otherwise
     */
    fun removeUsernamePasswordIdentity(
        actor: Admin,
        clock: Clock,
        correlationId: CorrelationId,
        username: String
    ): Either<Error, UserSourcingEvent> {
        return identities.firstOrNull { it is UsernamePasswordIdentity && it.username == username }
            .toOption()
            .toEither {
                Error.UserIdentityNotFound(
                    "Not able to find identity with username \"$username\".",
                    mapOf("username" to username),
                    correlationId
                )
            }
            .map {
                UsernamePasswordIdentityRemoved(
                    id,
                    aggregateVersion + 1,
                    actor.id,
                    clock.now(),
                    correlationId,
                    username
                )
            }
    }

    fun addWebauthnIdentity(
        actor: Member,
        clock: Clock,
        correlationId: CorrelationId,
        authenticator: Authenticator
    ): Either<Error, UserSourcingEvent> {
        return requireUserIsActor(actor, correlationId)
            .flatMap { requireUniqueCredentialId(authenticator.attestedCredentialData.credentialId, correlationId) }
            .map {
                WebauthnIdentityAdded(
                    id,
                    aggregateVersion + 1,
                    actor.id,
                    clock.now(),
                    correlationId,
                    authenticator
                )
            }
    }

    private fun requireUniqueCredentialId(
        credentialId: ByteArray,
        correlationId: CorrelationId
    ): Either<Error, Unit> {
        return identities.firstOrNull {
            it is WebauthnIdentity
                    && it.authenticator.attestedCredentialData.credentialId.contentEquals(credentialId)
        }
            .toOption()
            .map {
                Error.CredentialIdAlreadyInUse(
                    "Credential id is already in use.",
                    correlationId
                )
            }
            .toEither { }
            .swap()
    }

    private fun requireUserIsActor(
        actor: Member,
        correlationId: CorrelationId
    ): Either<Error, Unit> {
        return if (actor.userId != id) {
            Error.UserNotActor("User is not actor.", correlationId).left()
        } else {
            Unit.right()
        }
    }

    fun removeWebauthnIdentity(
        actor: Admin,
        clock: Clock,
        correlationId: CorrelationId,
        credentialId: ByteArray
    ): Either<Error, UserSourcingEvent> {
        return identities.firstOrNull {
            it is WebauthnIdentity
                    && it.authenticator.attestedCredentialData.credentialId.contentEquals(credentialId)
        }
            .toOption()
            .toEither {
                Error.UserIdentityNotFound(
                    "Not able to find identity with credentialId \"${credentialId.toHex()}\".",
                    mapOf("credentialId" to credentialId.toHex()),
                    correlationId
                )
            }
            .map {
                WebauthnIdentityRemoved(
                    id,
                    aggregateVersion + 1,
                    actor.id,
                    clock.now(),
                    correlationId,
                    credentialId
                )
            }
    }

    suspend fun addCardIdentity(
        actor: Admin,
        clock: Clock,
        correlationId: CorrelationId,
        cardId: String,
        cardSecret: String,
        gettingUserByCardId: GettingUserByCardId
    ): Either<Error, UserSourcingEvent> {
        return requireUniqueCardId(cardId, gettingUserByCardId, correlationId)
            .flatMap { CardIdentity.fromUnvalidated(cardId, cardSecret, correlationId) }
            .map {
                CardIdentityAdded(
                    id,
                    aggregateVersion + 1,
                    actor.id,
                    clock.now(),
                    correlationId,
                    it.cardId,
                    it.cardSecret
                )
            }
    }

    private suspend fun requireUniqueCardId(
        cardId: String,
        gettingUserByCardId: GettingUserByCardId,
        correlationId: CorrelationId
    ): Either<Error, Unit> {
        return gettingUserByCardId.getByCardId(cardId)
            .swap()
            .mapLeft {
                Error.CardIdAlreadyInUse(
                    "Card id is already in use.",
                    correlationId
                )
            }
            .flatMap {
                if (it is Error.UserNotFoundByCardId) {
                    Unit.right()
                } else {
                    it.left()
                }
            }
    }

    /**
     * Removes the user's identity given by the card id.
     *
     * @return error if no identity with given card id exists, sourcing event otherwise
     */
    fun removeCardIdentity(
        actor: Admin,
        clock: Clock,
        correlationId: CorrelationId,
        cardId: String
    ): Either<Error, UserSourcingEvent> {
        return identities.firstOrNull { it is CardIdentity && it.cardId == cardId }
            .toOption()
            .toEither {
                Error.UserIdentityNotFound(
                    "Not able to find identity with card id $cardId.",
                    mapOf("cardId" to cardId),
                    correlationId
                )
            }
            .map {
                CardIdentityRemoved(
                    id,
                    aggregateVersion + 1,
                    actor.id,
                    clock.now(),
                    correlationId,
                    cardId
                )
            }
    }

    suspend fun addPhoneNrIdentity(
        actor: Admin,
        clock: Clock,
        correlationId: CorrelationId,
        phoneNr: String,
        gettingUserByIdentity: GettingUserByIdentity
    ): Either<Error, UserSourcingEvent> {
        return requireUniquePhoneNr(phoneNr, gettingUserByIdentity, correlationId)
            .flatMap { PhoneNrIdentity.fromUnvalidated(phoneNr, correlationId) }
            .map {
                PhoneNrIdentityAdded(
                    id,
                    aggregateVersion + 1,
                    actor.id,
                    clock.now(),
                    correlationId,
                    it.phoneNr
                )
            }
    }

    private suspend fun requireUniquePhoneNr(
        phoneNr: String,
        gettingUserByIdentity: GettingUserByIdentity,
        correlationId: CorrelationId
    ): Either<Error, Unit> {
        return gettingUserByIdentity.getByIdentity(PhoneNrIdentity(phoneNr))
            .swap()
            .mapLeft {
                Error.PhoneNrAlreadyInUse(
                    "Phone number is already in use.",
                    correlationId
                )
            }
            .flatMap {
                if (it is Error.UserNotFoundByIdentity) {
                    Unit.right()
                } else {
                    it.left()
                }
            }
    }

    /**
     * Removes the user's identity given by the phone number.
     *
     * @return error if no identity with given card exists, sourcing event otherwise
     */
    fun removePhoneNrIdentity(
        actor: Admin,
        clock: Clock,
        correlationId: CorrelationId,
        phoneNr: String
    ): Either<Error, UserSourcingEvent> {
        return identities.firstOrNull { it is PhoneNrIdentity && it.phoneNr == phoneNr }
            .toOption()
            .toEither {
                Error.UserIdentityNotFound(
                    "Not able to find identity with phone number $phoneNr.",
                    mapOf("phoneNr" to phoneNr),
                    correlationId
                )
            }
            .map {
                PhoneNrIdentityRemoved(
                    id,
                    aggregateVersion + 1,
                    actor.id,
                    clock.now(),
                    correlationId,
                    phoneNr
                )
            }
    }

    fun addPinIdentity(
        actor: Admin,
        clock: Clock,
        correlationId: CorrelationId,
        pin: String
    ): Either<Error, UserSourcingEvent> {
        return requireNoPinIdentity(correlationId)
            .flatMap {
                PinIdentity.fromUnvalidated(pin, correlationId)
                    .map {
                        PinIdentityAdded(
                            id,
                            aggregateVersion + 1,
                            actor.id,
                            clock.now(),
                            correlationId,
                            pin
                        )
                    }
            }
    }

    private fun requireNoPinIdentity(
        correlationId: CorrelationId
    ): Either<Error, Unit> {
        return identities.firstOrNull { it is PinIdentity }
            .toOption()
            .map {
                Error.PinIdentityAlreadyFound(
                    "User already has a pin identity.",
                    correlationId
                )
            }
            .toEither { }
            .swap()
    }

    /**
     * Removes the user's pin identity.
     *
     * @return error if no pin identity exists, sourcing event otherwise
     */
    fun removePinIdentity(
        actor: Admin,
        clock: Clock,
        correlationId: CorrelationId,
    ): Either<Error, UserSourcingEvent> {
        return identities.firstOrNull { it is PinIdentity }
            .toOption()
            .toEither {
                Error.UserIdentityNotFound(
                    "Not able to find pin identity.",
                    mapOf(),
                    correlationId
                )
            }
            .map {
                PinIdentityRemoved(
                    id,
                    aggregateVersion + 1,
                    actor.id,
                    clock.now(),
                    correlationId
                )
            }
    }

    /**
     * Adds a member qualification given by its id to the user.
     *
     * @return error if the qualification cannot be found, sourcing event otherwise
     */
    suspend fun addMemberQualification(
        actor: Instructor,
        clock: Clock,
        correlationId: CorrelationId,
        qualificationId: QualificationId,
        gettingQualificationById: GettingQualificationById
    ): Either<Error, UserSourcingEvent> {
        return requireInstructorWithQualification(actor, qualificationId, correlationId)
            .flatMap {
                memberQualifications.firstOrNull { it == qualificationId }
                    .toOption()
                    .map {
                        Error.MemberQualificationAlreadyFound(
                            "User $id already has member qualification $qualificationId.",
                            qualificationId,
                            correlationId
                        )
                    }
                    .toEither { }
                    .swap()
            }
            .flatMap { gettingQualificationById.getQualificationById(qualificationId) }
            .map {
                MemberQualificationAdded(
                    id,
                    aggregateVersion + 1,
                    actor.id,
                    clock.now(),
                    correlationId,
                    qualificationId
                )
            }
    }

    private fun requireInstructorWithQualification(
        actor: Instructor,
        qualificationId: QualificationId,
        correlationId: CorrelationId
    ): Either<Error, Unit> {
        return if (!actor.hasQualification(qualificationId)) {
            Error.InstructorPermissionNotFound(
                "Actor not has instructor permission for qualification $qualificationId.",
                qualificationId,
                correlationId = correlationId
            ).left()
        } else {
            Unit.right()
        }
    }

    /**
     * Removes the user's member qualification given by the qualification id.
     *
     * @return error if the user does not have the member qualification, sourcing event otherwise
     */
    fun removeMemberQualification(
        actor: Admin,
        clock: Clock,
        correlationId: CorrelationId,
        qualificationId: QualificationId
    ): Either<Error, UserSourcingEvent> =
        removeMemberQualification(actor.id, clock, correlationId, qualificationId)

    /**
     * Removes the user's member qualification (triggered by a domain event).
     */
    internal fun removeMemberQualification(
        domainEvent: DomainEvent,
        clock: Clock,
        qualificationId: QualificationId
    ): Either<Error, UserSourcingEvent> =
        removeMemberQualification(domainEvent.actorId, clock, domainEvent.correlationId, qualificationId)

    private fun removeMemberQualification(
        actorId: ActorId,
        clock: Clock,
        correlationId: CorrelationId,
        qualificationId: QualificationId
    ): Either<Error, UserSourcingEvent> {
        return memberQualifications.firstOrNull { it == qualificationId }
            .toOption()
            .toEither {
                Error.MemberQualificationNotFound(
                    "Not able to find member qualification with id $qualificationId.",
                    qualificationId,
                    correlationId
                )
            }
            .map {
                MemberQualificationRemoved(
                    id,
                    aggregateVersion + 1,
                    actorId,
                    clock.now(),
                    correlationId,
                    qualificationId
                )
            }
    }

    /**
     * Adds an instructor qualification given by its id to the user.
     *
     * @return error if the qualification cannot be found, sourcing event otherwise
     */
    suspend fun addInstructorQualification(
        actor: Admin,
        clock: Clock,
        correlationId: CorrelationId,
        qualificationId: QualificationId,
        gettingQualificationById: GettingQualificationById
    ): Either<Error, UserSourcingEvent> {
        return instructorQualifications?.firstOrNull { it == qualificationId }
            .toOption()
            .map {
                Error.InstructorQualificationAlreadyFound(
                    "User $id already has instructor qualification $qualificationId.",
                    qualificationId,
                    correlationId
                )
            }
            .toEither { }
            .swap()
            .flatMap {
                gettingQualificationById.getQualificationById(qualificationId)
            }
            .map {
                InstructorQualificationAdded(
                    id,
                    aggregateVersion + 1,
                    actor.id,
                    clock.now(),
                    correlationId,
                    qualificationId
                )
            }
    }

    /**
     * Removes the user's instructor qualification given by the qualification id.
     *
     * @return error if the user does not have the instructor qualification, sourcing event otherwise
     */
    fun removeInstructorQualification(
        actor: Admin,
        clock: Clock,
        correlationId: CorrelationId,
        qualificationId: QualificationId
    ): Either<Error, UserSourcingEvent> =
        removeInstructorQualification(actor.id, clock, correlationId, qualificationId)

    /**
     * Removes the user's instructor qualification (triggered by a domain event).
     */
    internal fun removeInstructorQualification(
        domainEvent: DomainEvent,
        clock: Clock,
        qualificationId: QualificationId
    ): Either<Error, UserSourcingEvent> =
        removeInstructorQualification(domainEvent.actorId, clock, domainEvent.correlationId, qualificationId)

    private fun removeInstructorQualification(
        actorId: ActorId,
        clock: Clock,
        correlationId: CorrelationId,
        qualificationId: QualificationId
    ): Either<Error, UserSourcingEvent> {
        return instructorQualifications?.firstOrNull { it == qualificationId }
            .toOption()
            .toEither {
                Error.InstructorQualificationNotFound(
                    "Not able to find instructor qualification with id $qualificationId.",
                    qualificationId,
                    correlationId
                )
            }
            .map {
                InstructorQualificationRemoved(
                    id,
                    aggregateVersion + 1,
                    actorId,
                    clock.now(),
                    correlationId,
                    qualificationId
                )
            }
    }

    /**
     * Changes whether the user is admin or not.
     */
    fun changeIsAdmin(
        actor: Admin,
        clock: Clock,
        correlationId: CorrelationId,
        isAdmin: Boolean
    ): Either<Error, UserSourcingEvent> {
        return if (this.isAdmin == isAdmin) {
            if (isAdmin) {
                Error.UserAlreadyAdmin("User already is admin.", correlationId).left()
            } else {
                Error.UserAlreadyNotAdmin("User already not is admin.", correlationId).left()
            }
        } else {
            IsAdminChanged(
                id,
                aggregateVersion + 1,
                actor.id,
                clock.now(),
                correlationId,
                isAdmin
            ).right()
        }
    }

    fun delete(
        actor: Admin,
        clock: Clock,
        correlationId: CorrelationId
    ): Either<Error, UserSourcingEvent> {
        return requireUserIsNotActor(actor, correlationId)
            .map { UserDeleted(id, aggregateVersion + 1, actor.id, clock.now(), correlationId) }
    }

    private fun requireUserIsNotActor(
        actor: Admin,
        correlationId: CorrelationId
    ): Either<Error, Unit> {
        return if (actor.userId == id) {
            Error.UserIsActor("User is actor and cannot lock/delete themselves.", correlationId).left()
        } else {
            Unit.right()
        }
    }

    fun hasIdentity(userIdentity: UserIdentity) = identities.contains(userIdentity)

    fun hasUsername(username: String) =
        identities.any { it is UsernamePasswordIdentity && it.username == username }

    fun hasCardId(cardId: String) =
        identities.any { it is CardIdentity && it.cardId == cardId }

    fun asMember(): Member = Member(id, name, memberQualifications)

    fun asInstructor(): Either<Error, Instructor> =
        Option.fromNullable(instructorQualifications)
            .map { Instructor(id, name, it) }
            .toEither { Error.UserNotInstructor("User $id is not an instructor.") }

    fun asAdmin(): Either<Error, Admin> =
        if (!isAdmin) {
            Error.UserNotAdmin("User $id is not an admin.").left()
        } else {
            Admin(id, name).right()
        }


    class EventHistoryDoesNotStartWithUserCreated(message: String) : Exception(message)
}