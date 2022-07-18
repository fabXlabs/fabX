package cloud.fabX.fabXaccess.user.model

import arrow.core.Either
import arrow.core.Option
import cloud.fabX.fabXaccess.common.model.Error

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