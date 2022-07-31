package cloud.fabX.fabXaccess.device.model

import cloud.fabX.fabXaccess.common.model.Identity

/**
 * Identifying a Device.
 */
interface DeviceIdentity : Identity

/**
 * Identifying a Device by mac and secret.
 */
data class MacSecretIdentity(val mac: String, val secret: String) : DeviceIdentity