package cloud.fabX.fabXaccess.device.application

import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.SystemActor
import cloud.fabX.fabXaccess.device.model.DevicePinStatus
import cloud.fabX.fabXaccess.device.model.DevicePinStatusRepository

class GettingDevicePinStatus(
    loggerFactory: LoggerFactory,
    private val devicePinStatusRepository: DevicePinStatusRepository
) {
    private val log = loggerFactory.invoke(this::class.java)

    suspend fun getAll(
        actor: SystemActor,
        correlationId: CorrelationId
    ): Set<DevicePinStatus> {
        // no logging as it is used by metrics endpoint
        return devicePinStatusRepository.getAll()
    }
}
