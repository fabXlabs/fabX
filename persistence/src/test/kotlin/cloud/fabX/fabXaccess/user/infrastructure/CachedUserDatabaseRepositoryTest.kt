package cloud.fabX.fabXaccess.user.infrastructure

import cloud.fabX.fabXaccess.common.infrastructure.withTestApp
import org.kodein.di.instance

internal class CachedUserDatabaseRepositoryTest : UserDatabaseRepositoryTest() {
    override fun withRepository(block: suspend (UserDatabaseRepository) -> Unit) = withTestApp { di ->
        val repository: CachedUserDatabaseRepository by di.instance()
        block(repository)
    }
}