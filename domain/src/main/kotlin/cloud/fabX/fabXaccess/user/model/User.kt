package cloud.fabX.fabXaccess.user.model

import arrow.core.Either
import arrow.core.Option
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.Entity
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.qualification.model.QualificationId

data class User(
    override val id: UserId,
    val firstName: String,
    val lastName: String,
    val wikiName: String,
    val phoneNumber: String?,
    val locked: Boolean,
    val notes: String?,
    private val memberQualifications: List<QualificationId>,
    private val instructorQualifications: List<QualificationId>?,
    private val isAdmin: Boolean
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

    fun asMember(): Member = Member(id, memberQualifications)

    fun asInstructor(): Either<Error, Instructor> =
        Option.fromNullable(instructorQualifications)
            .map { Instructor(id, it) }
            .toEither { Error.UserNotInstructor("User $id is not an instructor.") }

    fun asAdmin(): Either<Error, Admin> = Either.conditionally(
        isAdmin,
        { Error.UserNotAdmin("User $id is not an admin.") },
        { Admin(id) }
    )
}