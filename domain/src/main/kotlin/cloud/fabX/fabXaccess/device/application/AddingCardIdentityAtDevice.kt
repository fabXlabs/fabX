package cloud.fabX.fabXaccess.device.application

import arrow.core.Either
import arrow.core.flatMap
import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.CardCreatedAtDevice
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.DeviceId
import cloud.fabX.fabXaccess.common.model.DomainEventPublisher
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.UserId
import cloud.fabX.fabXaccess.device.model.CreateCardAtDevice
import cloud.fabX.fabXaccess.device.model.DeviceRepository
import cloud.fabX.fabXaccess.user.model.Admin
import cloud.fabX.fabXaccess.user.model.GettingUserById
import kotlin.random.Random
import kotlinx.datetime.Clock

/**
 * Service to add a card identity to a User at a Device.
 */
class AddingCardIdentityAtDevice(
    loggerFactory: LoggerFactory,
    private val domainEventPublisher: DomainEventPublisher,
    private val deviceRepository: DeviceRepository,
    private val gettingUserById: GettingUserById,
    private val createCardAtDevice: CreateCardAtDevice,
    private val clock: Clock
) {
    private val log = loggerFactory.invoke(this::class.java)

    suspend fun addCardIdentityAtDevice(
        actor: Admin,
        correlationId: CorrelationId,
        deviceId: DeviceId,
        userId: UserId
    ): Either<Error, Unit> {
        val cardSecret = generateRandomCardSecret()
        return deviceRepository.getById(deviceId)
            .flatMap { device ->
                gettingUserById.getUserById(userId)
                    .flatMap { user ->
                        createCardAtDevice
                            .createCard(
                                deviceId,
                                correlationId,
                                userName = "${user.firstName} ${user.lastName}",
                                cardSecret = cardSecret
                            )
                    }
            }
            .map { cardId ->
                domainEventPublisher.publish(
                    CardCreatedAtDevice(
                        actorId = actor.id,
                        timestamp = clock.now(),
                        correlationId = correlationId,
                        userId = userId,
                        cardId = cardId,
                        cardSecret = cardSecret
                    )
                )
            }
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun generateRandomCardSecret(): String = Random
        .nextBytes(32)
        .toHexString(HexFormat.UpperCase)
}