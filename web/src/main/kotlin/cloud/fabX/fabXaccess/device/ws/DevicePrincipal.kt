package cloud.fabX.fabXaccess.device.ws

import cloud.fabX.fabXaccess.device.model.Device
import io.ktor.server.auth.Principal

data class DevicePrincipal(val device: Device) : Principal