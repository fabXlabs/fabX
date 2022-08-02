package cloud.fabX.fabXaccess.device.model

import arrow.core.Either
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.DeviceId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.ToolId

fun interface UnlockingToolAtDevice {
    suspend fun unlockTool(deviceId: DeviceId, toolId: ToolId, correlationId: CorrelationId): Either<Error, Unit>
}