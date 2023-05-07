package cloud.fabX.fabXaccess.qualification.infrastructure

import cloud.fabX.fabXaccess.common.infrastructure.withTestApp
import org.kodein.di.instance

internal class CachedQualificationDatabaseRepositoryTest : QualificationDatabaseRepositoryTest() {
    override fun withRepository(block: suspend (QualificationDatabaseRepository) -> Unit) = withTestApp { di ->
        val repository: CachedQualificationDatabaseRepository by di.instance()
        block(repository)
    }
}