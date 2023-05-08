package cloud.fabX.fabXaccess.device.infrastructure

import cloud.fabX.fabXaccess.common.infrastructure.withTestApp
import org.kodein.di.instance

internal class CachedDeviceDatabaseRepositoryTest : DeviceDatabaseRepositoryTest() {
    override fun withRepository(block: suspend (DeviceDatabaseRepository) -> Unit) = withTestApp { di ->
        val repository: CachedDeviceDatabaseRepository by di.instance()
        block(repository)
    }
}
