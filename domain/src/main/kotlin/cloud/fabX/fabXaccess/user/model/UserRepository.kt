package cloud.fabX.fabXaccess.user.model

import arrow.core.Either
import arrow.core.Option
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.QualificationId
import cloud.fabX.fabXaccess.common.model.UserId

interface UserRepository {
    suspend fun getAll(): Set<User>
    suspend fun getById(id: UserId): Either<Error, User>
    suspend fun getSourcingEvents(): List<UserSourcingEvent>
    suspend fun store(event: UserSourcingEvent): Option<Error>
}

fun interface GettingUserByIdentity {
    suspend fun getByIdentity(identity: UserIdentity): Either<Error, User>
}

fun interface GettingUserByUsername {
    suspend fun getByUsername(username: String): Either<Error, User>
}

fun interface GettingUserByCardId {
    suspend fun getByCardId(cardId: String): Either<Error, User>
}

fun interface GettingUserByWikiName {
    suspend fun getByWikiName(wikiName: String): Either<Error, User>
}

fun interface GettingUsersByMemberQualification {
    suspend fun getByMemberQualification(qualificationId: QualificationId): Set<User>
}

fun interface GettingUsersByInstructorQualification {
    suspend fun getByInstructorQualification(qualificationId: QualificationId): Set<User>
}