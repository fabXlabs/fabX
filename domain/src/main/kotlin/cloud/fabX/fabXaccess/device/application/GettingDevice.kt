package cloud.fabX.fabXaccess.device.application

import arrow.core.Either
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.application.logger
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.DeviceId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.device.model.Device
import cloud.fabX.fabXaccess.user.model.Admin

/**
 * Service to get devices.
 */
class GettingDevice {

    private val log = logger()
    private val deviceRepository = DomainModule.deviceRepository()

    fun getAll(
        actor: Admin,
        correlationId: CorrelationId
    ): Set<Device> {
        log.debug("getAll (actor: $actor, correlationId: $correlationId)...")

        return deviceRepository.getAll()
    }

    fun getById(
        actor: Admin,
        correlationId: CorrelationId,
        deviceId: DeviceId
    ): Either<Error, Device> {
        log.debug("getById (actor: $actor, correlationId: $correlationId)...")

        return deviceRepository.getById(deviceId)
    }
}