package cloud.fabX.fabXaccess.user.model

import cloud.fabX.fabXaccess.qualification.model.QualificationId
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFixture

object UserFixture {

    fun arbitraryUser(
        userId: UserId = UserIdFixture.arbitraryId(),
        aggregateVersion: Long = 1,
        firstName: String = "first",
        lastName: String = "last",
        wikiName: String = "wiki",
        locked: Boolean = false,
        notes: String? = null,
        identities: Set<UserIdentity> = setOf(UsernamePasswordIdentity("firstlast", "supersecret")),
        memberQualifications: Set<QualificationId> = setOf(QualificationIdFixture.arbitraryId()),
        instructorQualifications: Set<QualificationId>? = null,
        isAdmin: Boolean = false
    ): User = User(
        userId,
        aggregateVersion,
        firstName,
        lastName,
        wikiName,
        locked,
        notes,
        identities,
        memberQualifications,
        instructorQualifications,
        isAdmin
    )

    fun userWithIdentity(
        identity: UserIdentity,
        userId: UserId = UserIdFixture.arbitraryId(),
        aggregateVersion: Long = 1,
        firstName: String = "first",
        lastName: String = "last",
        wikiName: String = "wiki",
        locked: Boolean = false,
        notes: String? = null,
        memberQualifications: Set<QualificationId> = setOf(QualificationIdFixture.arbitraryId()),
        instructorQualifications: Set<QualificationId>? = null,
        isAdmin: Boolean = false
    ): User = arbitraryUser(
        userId,
        aggregateVersion,
        firstName,
        lastName,
        wikiName,
        locked,
        notes,
        setOf(identity),
        memberQualifications,
        instructorQualifications,
        isAdmin,
    )
}