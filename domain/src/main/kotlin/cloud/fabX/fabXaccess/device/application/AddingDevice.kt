package cloud.fabX.fabXaccess.device.application

import arrow.core.Either
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.DeviceId
import cloud.fabX.fabXaccess.common.model.DeviceIdFactory
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.device.model.Device
import cloud.fabX.fabXaccess.device.model.DeviceRepository
import cloud.fabX.fabXaccess.device.model.MacSecretIdentity
import cloud.fabX.fabXaccess.user.model.Admin

/**
 * Service to add new devices.
 */
class AddingDevice(
    loggerFactory: LoggerFactory,
    private val deviceRepository: DeviceRepository,
    private val deviceIdFactory: DeviceIdFactory
) {
    private val log = loggerFactory.invoke(this::class.java)

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
            deviceIdFactory,
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