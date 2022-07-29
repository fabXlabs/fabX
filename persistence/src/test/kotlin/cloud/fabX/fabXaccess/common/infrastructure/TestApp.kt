package cloud.fabX.fabXaccess.common.infrastructure

import cloud.fabX.fabXaccess.persistenceModule
import org.kodein.di.DI

internal fun withTestApp(
    diSetup: DI.MainBuilder.() -> Unit,
    block: (DI) -> Unit
) {
    val testApp = DI {
        import(persistenceModule)
        diSetup()
    }

    block(testApp)
}