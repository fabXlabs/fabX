package cloud.fabX.fabXaccess.device.application

import arrow.core.Either
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.application.logger
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.device.model.DeviceActor
import cloud.fabX.fabXaccess.device.model.DeviceConfiguration

/**
 * Service for a device to get its configuration.
 */
class GettingConfiguration {

    private val log = logger()
    private val deviceRepository = DomainModule.deviceRepository()

    fun getConfiguration(
        actor: DeviceActor
    ): Either<Error, DeviceConfiguration> {
        log.debug("getConfiguration...")

        return deviceRepository
            .getById(actor.deviceId)
            .map { it.getConfiguration() }
            .tap { log.debug("...getConfiguration successful") }
            .tapLeft { log.error("...getConfiguration error: $it") }
    }
}