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
    val lockedReason: String?
) : Entity<UserId> {

    fun changeValues(firstName: ChangeableValue<String>): UserSourcingEvent {
        return UserValuesChanged(id, firstName)
    }

    override fun toString(): String {
        return "User(" +
                "id=$id, " +
                "firstName='$firstName', " +
                "lastName='$lastName', " +
                "wikiName='$wikiName', " +
                "phoneNumber='$phoneNumber', " +
                "locked=$locked, " +
                "lockedReason=$lockedReason" +
                ")"
    }
}