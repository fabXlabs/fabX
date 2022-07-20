package cloud.fabX.fabXaccess.device.application

import arrow.core.Option
import arrow.core.flatMap
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.application.logger
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.device.model.DeviceId
import cloud.fabX.fabXaccess.user.model.Admin

/**
 * Service for detaching a tool from a device.
 */
class DetachingTool {

    private val log = logger()
    private val deviceRepository = DomainModule.deviceRepository()

    fun detachTool(
        actor: Admin,
        deviceId: DeviceId,
        pin: Int
    ): Option<Error> {
        log.debug("detachTool...")

        return deviceRepository.getById(deviceId)
            .flatMap {
                it.detachTool(
                    actor,
                    pin
                )
            }
            .flatMap {
                deviceRepository.store(it)
                    .toEither { }
                    .swap()
            }
            .swap()
            .orNone()
            .tapNone { log.debug("...detachTool done") }
            .tap { log.error("...detachTool error: $it") }
    }
}