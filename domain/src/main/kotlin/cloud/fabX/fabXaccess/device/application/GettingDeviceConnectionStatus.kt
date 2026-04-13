package cloud.fabX.fabXaccess.device.application

import arrow.core.Either
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.DeviceId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.device.model.DeviceRepository
import cloud.fabX.fabXaccess.device.model.GetDeviceConnectionStatus
import cloud.fabX.fabXaccess.user.model.Member

/**
 * Service to get device connection status.
 */
class GettingDeviceConnectionStatus(
    loggerFactory: LoggerFactory,
    private val deviceRepository: DeviceRepository,
    private val getDeviceConnectionStatus: GetDeviceConnectionStatus
) {
    private val log = loggerFactory.invoke(this::class.java)

    suspend fun getAll(
        actor: Member,
        correlationId: CorrelationId
    ): Map<DeviceId, Boolean> {
        log.debug("getAll (actor: $actor, correlationId: $correlationId)...")

        return deviceRepository.getAll()
            .associate { device ->
                Pair(device.id, getDeviceConnectionStatus.isConnected(device.id))
            }
    }

    suspend fun getById(
        actor: Member,
        correlationId: CorrelationId,
        deviceId: DeviceId
    ): Either<Error, Boolean> =
        log.logError(actor, correlationId, "getById") {
            deviceRepository.getById(deviceId)
                .map { device ->
                    getDeviceConnectionStatus.isConnected(device.id)
                }
        }
}
