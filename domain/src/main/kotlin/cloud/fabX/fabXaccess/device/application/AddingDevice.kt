package cloud.fabX.fabXaccess.device.application

import arrow.core.Either
import arrow.core.flatMap
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.DeviceId
import cloud.fabX.fabXaccess.common.model.DeviceIdFactory
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.device.model.Device
import cloud.fabX.fabXaccess.device.model.DeviceRepository
import cloud.fabX.fabXaccess.user.model.Admin
import kotlinx.datetime.Clock

/**
 * Service to add new devices.
 */
class AddingDevice(
    loggerFactory: LoggerFactory,
    private val deviceRepository: DeviceRepository,
    private val deviceIdFactory: DeviceIdFactory,
    private val clock: Clock
) {
    private val log = loggerFactory.invoke(this::class.java)

    suspend fun addDevice(
        actor: Admin,
        correlationId: CorrelationId,
        name: String,
        background: String,
        backupBackendUrl: String,
        mac: String,
        secret: String
    ): Either<Error, DeviceId> {
        log.debug("addDevice...")

        return Device.addNew(
            deviceIdFactory,
            actor,
            clock,
            correlationId,
            name,
            background,
            backupBackendUrl,
            mac,
            secret
        ).flatMap { sourcingEvent ->
            deviceRepository
                .store(sourcingEvent)
                .map { sourcingEvent.aggregateRootId }
        }
            .onRight { log.debug("...addDevice done") }
            .onLeft { log.error("...addDevice error: $it") }
    }
}