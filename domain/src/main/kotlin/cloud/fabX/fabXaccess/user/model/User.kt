package cloud.fabX.fabXaccess.user.model

import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.Entity

data class User(
    override val id: UserId,
    val firstName: String,
    val lastName: String,
    val wikiName: String,
    val phoneNumber: String?,
    val locked: Boolean,
    val notes: String?
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
}