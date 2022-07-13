package cloud.fabX.fabXaccess.device.application

import arrow.core.Option
import arrow.core.flatMap
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.application.logger
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.device.model.DeviceId
import cloud.fabX.fabXaccess.user.model.Admin


// TODO CreatingDevice service
// TODO DeletingDevice service

/**
 * Service to handle changing device properties.
 */
class ChangingDevice {

    private val log = logger()
    private val deviceRepository = DomainModule.deviceRepository()

    fun changeDeviceDetails(
        actor: Admin,
        deviceId: DeviceId,
        name: ChangeableValue<String>,
        background: ChangeableValue<String>,
        backupBackendUrl: ChangeableValue<String>
    ): Option<Error> {
        log.debug("changeDeviceDetails...")

        return deviceRepository.getById(deviceId)
            .map {
                it.changeDetails(actor, name, background, backupBackendUrl)
            }
            .flatMap {
                deviceRepository.store(it)
                    .toEither { }
                    .swap()
            }
            .swap()
            .orNone()
            .tapNone { log.debug("...changeDeviceDetails done") }
            .tap { log.error("...changeDeviceDetails error: $it") }
    }

}