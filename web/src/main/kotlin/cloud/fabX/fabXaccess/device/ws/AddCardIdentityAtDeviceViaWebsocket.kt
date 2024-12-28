package cloud.fabX.fabXaccess.device.ws

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.DeviceId
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.device.model.CreateCardAtDevice

class AddCardIdentityAtDeviceViaWebsocket(
    private val deviceWebsocketController: DeviceWebsocketController
) : CreateCardAtDevice {
    override suspend fun createCard(
        deviceId: DeviceId,
        correlationId: CorrelationId,
        userName: String,
        cardSecret: String
    ): Either<Error, String> {
        val commandId = deviceWebsocketController.newCommandId()

        return deviceWebsocketController.setupReceivingDeviceResponse(deviceId, commandId, correlationId)
            .flatMap {
                deviceWebsocketController.sendCommand(
                    deviceId,
                    CreateCard(commandId, userName, cardSecret),
                    correlationId
                )
            }
            .flatMap {
                deviceWebsocketController.receiveDeviceResponse(deviceId, commandId, correlationId, timeoutMs = 15_000)
            }
            .flatMap {
                if (it !is CardCreationResponse) {
                    Error.UnexpectedDeviceResponse(
                        "Unexpected device response type.",
                        deviceId,
                        it.toString(),
                        correlationId
                    ).left()
                } else {
                    it.cardId.right()
                }
            }
    }
}