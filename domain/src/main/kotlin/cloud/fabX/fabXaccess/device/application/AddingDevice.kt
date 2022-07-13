package cloud.fabX.fabXaccess.device.application

import arrow.core.Option
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.application.logger
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.device.model.Device
import cloud.fabX.fabXaccess.user.model.Admin

/**
 * Service to add new devices.
 */
class AddingDevice {

    private val log = logger()
    private val deviceRepository = DomainModule.deviceRepository()

    fun addDevice(
        actor: Admin,
        name: String,
        background: String,
        backupBackendUrl: String
    ): Option<Error> {
        log.debug("createDevice...")

        return deviceRepository
            .store(
                Device.addNew(
                    actor,
                    name,
                    background,
                    backupBackendUrl
                )
            )
            .tapNone { log.debug("...createDevice done") }
            .tap { log.error("...createDevice error: $it") }
    }
}