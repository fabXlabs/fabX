package cloud.fabX.fabXaccess.device.application

import arrow.core.Either
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.DeviceId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.device.model.DeviceRepository
import cloud.fabX.fabXaccess.user.model.Admin
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Service for detaching an input from a device.
 */
@OptIn(ExperimentalTime::class)
class DetachingInput(
    loggerFactory: LoggerFactory,
    private val deviceRepository: DeviceRepository,
    private val clock: Clock
) {
    private val log = loggerFactory.invoke(this::class.java)

    suspend fun detachInput(
        actor: Admin,
        correlationId: CorrelationId,
        deviceId: DeviceId,
        pin: Int
    ): Either<Error, Unit> =
        deviceRepository.getAndStoreFlatMap(deviceId, actor, correlationId, log, "detachInput") {
            it.detachInput(actor, clock, correlationId, pin)
        }
}
