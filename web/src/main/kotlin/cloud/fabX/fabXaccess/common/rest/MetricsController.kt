package cloud.fabX.fabXaccess.common.rest

import cloud.fabX.fabXaccess.common.model.SystemActor
import cloud.fabX.fabXaccess.common.model.newCorrelationId
import cloud.fabX.fabXaccess.device.application.GettingDevice
import cloud.fabX.fabXaccess.device.ws.DeviceWebsocketController
import cloud.fabX.fabXaccess.user.application.UserMetrics
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.micrometer.core.instrument.MultiGauge
import io.micrometer.core.instrument.MultiGauge.Row
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.Tags
import io.micrometer.prometheus.PrometheusMeterRegistry
import kotlinx.coroutines.runBlocking

class MetricsController(
    private val appMicrometerRegistry: PrometheusMeterRegistry,
    private val userMetrics: UserMetrics,
    private val gettingDevice: GettingDevice,
    private val deviceWebsocketController: DeviceWebsocketController
) {
    private lateinit var connectedDevicesMultiGauge: MultiGauge

    init {
        configureMetrics()
    }

    val routes: Route.() -> Unit = {
        get("/metrics") {
            updateMultiGaugeMetrics()
            call.respond(appMicrometerRegistry.scrape())
        }
    }

    private fun configureMetrics() {
        appMicrometerRegistry.gauge("fabx.users.amount", Unit) {
            runBlocking {
                userMetrics.getUserAmount(SystemActor).toDouble()
            }
        }

        connectedDevicesMultiGauge = MultiGauge.builder("fabx.devices.connected")
            .description("1 if a device is connected, 0 if not")
            .register(appMicrometerRegistry)
    }

    /**
     * Update some multi-gauge metrics on scrape.
     */
    private suspend fun updateMultiGaugeMetrics() {
        val connectedDevices = gettingDevice.getAll(SystemActor, newCorrelationId())
            .map {
                Row.of(
                    Tags.of(Tag.of("deviceId", it.id.serialize())),
                    if (deviceWebsocketController.isConnected(it.id)) {
                        1
                    } else {
                        0
                    }
                )
            }

        connectedDevicesMultiGauge.register(connectedDevices, true)
    }
}