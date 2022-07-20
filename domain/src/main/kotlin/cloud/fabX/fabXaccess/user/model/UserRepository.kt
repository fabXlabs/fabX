package cloud.fabX.fabXaccess.user.model

import arrow.core.Either
import arrow.core.Option
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.QualificationId
import cloud.fabX.fabXaccess.common.model.UserId

interface UserRepository {
    fun getAll(): Set<User>
    fun getById(id: UserId): Either<Error, User>
    fun store(event: UserSourcingEvent): Option<Error>
}

fun interface GettingUserByIdentity {
    fun getByIdentity(identity: UserIdentity): Either<Error, User>
}

fun interface GettingUserByUsername {
    fun getByUsername(username: String): Either<Error, User>
}

fun interface GettingUserByCardId {
    fun getByCardId(cardId: String): Either<Error, User>
}

fun interface GettingUserByWikiName {
    fun getByWikiName(wikiName: String): Either<Error, User>
}

fun interface GettingUsersByMemberQualification {
    fun getByMemberQualification(qualificationId: QualificationId): Set<User>
}

fun interface GettingUsersByInstructorQualification {
    fun getByInstructorQualification(qualificationId: QualificationId): Set<User>
}