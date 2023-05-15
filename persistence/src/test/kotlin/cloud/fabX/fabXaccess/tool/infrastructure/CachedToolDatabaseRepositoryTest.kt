package cloud.fabX.fabXaccess.tool.infrastructure

import cloud.fabX.fabXaccess.common.infrastructure.withTestApp
import org.kodein.di.instance

internal class CachedToolDatabaseRepositoryTest : ToolDatabaseRepositoryTest() {
    override fun withRepository(block: suspend (ToolDatabaseRepository) -> Unit) = withTestApp { di ->
        val repository: CachedToolDatabaseRepository by di.instance()
        block(repository)
    }
}