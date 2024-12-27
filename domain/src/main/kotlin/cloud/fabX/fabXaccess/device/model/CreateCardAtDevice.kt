package cloud.fabX.fabXaccess.device.model

import arrow.core.Either
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.DeviceId
import cloud.fabX.fabXaccess.common.model.Error

fun interface CreateCardAtDevice {
    /**
     * @return card id
     */
    suspend fun createCard(
        deviceId: DeviceId,
        correlationId: CorrelationId,
        userName: String,
        cardSecret: String
    ): Either<Error, String>
}