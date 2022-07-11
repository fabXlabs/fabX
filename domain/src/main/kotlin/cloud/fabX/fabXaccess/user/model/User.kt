package cloud.fabX.fabXaccess.user.model

import arrow.core.Either
import arrow.core.Option
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.Entity
import cloud.fabX.fabXaccess.common.model.Error

data class User(
    override val id: UserId,
    val firstName: String,
    val lastName: String,
    val wikiName: String,
    val phoneNumber: String?,
    val locked: Boolean,
    val notes: String?,
    private val member: Member,
    private val instructor: Instructor?,
    private val admin: Admin?
) : Entity<UserId> {

    fun changePersonalInformation(
        firstName: ChangeableValue<String> = ChangeableValue.LeaveAsIs,
        lastName: ChangeableValue<String> = ChangeableValue.LeaveAsIs,
        wikiName: ChangeableValue<String> = ChangeableValue.LeaveAsIs,
        phoneNumber: ChangeableValue<String?> = ChangeableValue.LeaveAsIs
    ): UserSourcingEvent {
        return UserPersonalInformationChanged(id, firstName, lastName, wikiName, phoneNumber)
    }

    fun changeLockState(
        locked: ChangeableValue<Boolean> = ChangeableValue.LeaveAsIs,
        notes: ChangeableValue<String?> = ChangeableValue.LeaveAsIs
    ): UserSourcingEvent {
        return UserLockStateChanged(id, locked, notes)
    }

    fun asMember(): Member = member

    fun asInstructor(): Either<Error, Instructor> =
        Option.fromNullable(instructor)
            .toEither { Error.UserNotInstructor("User $id is not an instructor.") }

    fun asAdmin(): Either<Error, Admin> =
        Option.fromNullable(admin)
            .toEither { Error.UserNotAdmin("User $id is not an admin.") }
}