package cloud.fabX.fabXaccess.user.infrastructure

import arrow.core.Either
import arrow.core.Option
import arrow.core.left
import arrow.core.right
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.user.model.User
import cloud.fabX.fabXaccess.user.model.UserSourcingEvent
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jetbrains.exposed.sql.Database

class CachedUserDatabaseRepository(db: Database) : UserDatabaseRepository(db) {

    private val cacheMutex = Mutex()

    private var cache: MutableMap<UserId, User>? = null
    private val outdated: MutableSet<UserId> = mutableSetOf()

    override suspend fun getAll(): Set<User> {
        cacheMutex.withLock {
            if (cache == null) {
                cache = super.getAll().associateBy { it.id }.toMutableMap()
            }
            if (outdated.size <= 1) {
                outdated.forEach { id ->
                    super.getById(id)
                        .onRight { cache!![id] = it }
                }
            } else {
                cache = super.getAll().associateBy { it.id }.toMutableMap()
            }
            outdated.clear()
        }
        return cache!!.values.toSet()
    }

    override suspend fun getById(id: UserId): Either<Error, User> {
        cacheMutex.withLock {
            if (cache == null) {
                cache = super.getAll().associateBy { it.id }.toMutableMap()
            } else if (outdated.contains(id)) {
                super.getById(id)
                    .onRight { cache!![id] = it }
                outdated.remove(id)
            }
        }
        return cache!![id]?.right()
            ?: Error.UserNotFound(
                "User with id $id not found.",
                id
            ).left()
    }

    override suspend fun store(event: UserSourcingEvent): Option<Error> {
        cacheMutex.withLock {
            outdated.add(event.aggregateRootId)
            cache?.remove(event.aggregateRootId)
        }
        return super.store(event)
    }
}