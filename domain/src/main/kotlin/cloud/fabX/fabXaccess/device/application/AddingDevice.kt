package cloud.fabX.fabXaccess.device.application

import arrow.core.Either
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.application.logger
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.DeviceId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.device.model.Device
import cloud.fabX.fabXaccess.device.model.MacSecretIdentity
import cloud.fabX.fabXaccess.user.model.Admin

/**
 * Service to add new devices.
 */
class AddingDevice {

    private val log = logger()
    private val deviceRepository = DomainModule.deviceRepository()

    fun addDevice(
        actor: Admin,
        correlationId: CorrelationId,
        name: String,
        background: String,
        backupBackendUrl: String,
        identity: MacSecretIdentity
    ): Either<Error, DeviceId> {
        log.debug("addDevice...")

        val sourcingEvent = Device.addNew(
            actor,
            correlationId,
            name,
            background,
            backupBackendUrl,
            identity
        )

        return deviceRepository
            .store(sourcingEvent)
            .toEither { }
            .swap()
            .map { sourcingEvent.aggregateRootId }
            .tap { log.debug("...addDevice done") }
            .tapLeft { log.error("...addDevice error: $it") }
    }
}