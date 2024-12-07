package cloud.fabX.fabXaccess.device.ws

import cloud.fabX.fabXaccess.common.rest.Principal
import cloud.fabX.fabXaccess.device.model.Device

data class DevicePrincipal(val device: Device) : Principal
