package cloud.fabX.fabXaccess.device.ws

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.DeviceId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.device.model.UpdateDeviceFirmware

/**
 * _Triggers_ a device firmware update.
 */
class UpdateDeviceFirmwareViaWebsocket(
    private val deviceWebsocketController: DeviceWebsocketController
) : UpdateDeviceFirmware {

    override suspend fun updateDeviceFirmware(deviceId: DeviceId, correlationId: CorrelationId): Either<Error, Unit> {
        val commandId = deviceWebsocketController.newCommandId()

        return deviceWebsocketController.setupReceivingDeviceResponse(deviceId, commandId, correlationId)
            .flatMap {
                deviceWebsocketController.sendCommand(
                    deviceId,
                    UpdateDeviceFirmware(commandId),
                    correlationId
                )
            }
            .flatMap {
                deviceWebsocketController.receiveDeviceResponse(deviceId, commandId, correlationId)
            }
            .flatMap {
                if (it !is UpdateFirmwareResponse) {
                    Error.UnexpectedDeviceResponse(
                        "Unexpected device response type.",
                        deviceId,
                        it.toString(),
                        correlationId
                    ).left()
                } else {
                    Unit.right()
                }
            }
    }
}