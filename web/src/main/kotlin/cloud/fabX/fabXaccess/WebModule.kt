package cloud.fabX.fabXaccess

import cloud.fabX.fabXaccess.common.model.DeviceId
import cloud.fabX.fabXaccess.common.model.ToolId
import cloud.fabX.fabXaccess.common.rest.MetricsController
import cloud.fabX.fabXaccess.common.rest.MicrometerTaggedCounter
import cloud.fabX.fabXaccess.device.rest.DeviceController
import cloud.fabX.fabXaccess.device.rest.DeviceFirmwareUpdateController
import cloud.fabX.fabXaccess.device.ws.DeviceCommandHandlerImpl
import cloud.fabX.fabXaccess.device.ws.DeviceNotificationHandlerImpl
import cloud.fabX.fabXaccess.device.ws.DeviceWebsocketController
import cloud.fabX.fabXaccess.device.ws.RestartDeviceViaWebsocket
import cloud.fabX.fabXaccess.device.ws.UnlockToolAtDeviceViaWebsocket
import cloud.fabX.fabXaccess.device.ws.UpdateDeviceFirmwareViaWebsocket
import cloud.fabX.fabXaccess.qualification.rest.QualificationController
import cloud.fabX.fabXaccess.tool.rest.ToolController
import cloud.fabX.fabXaccess.user.application.WebauthnIdentityService
import cloud.fabX.fabXaccess.user.rest.AuthenticationService
import cloud.fabX.fabXaccess.user.rest.LoginController
import cloud.fabX.fabXaccess.user.rest.UserController
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import org.kodein.di.DI
import org.kodein.di.bindEagerSingleton
import org.kodein.di.bindSingleton
import org.kodein.di.instance

val webModule = DI.Module("web") {
    bindSingleton { AuthenticationService(instance(), instance(), instance()) }

    bindSingleton { DeviceCommandHandlerImpl(instance(), instance(), instance(), instance()) }
    bindSingleton { DeviceNotificationHandlerImpl(instance(), instance(), instance()) }

    bindSingleton { QualificationController(instance(), instance(), instance(), instance()) }
    bindSingleton { ToolController(instance(), instance(), instance(), instance()) }
    bindSingleton {
        DeviceController(
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance()
        )
    }
    bindSingleton {
        DeviceWebsocketController(
            instance(),
            instance(),
            instance(),
            instance(tag = "deviceReceiveTimeoutMillis")
        )
    }
    bindSingleton {
        DeviceFirmwareUpdateController(
            instance(),
            instance(),
            instance(tag = "firmwareDirectory")
        )
    }
    bindSingleton {
        UserController(
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance()
        )
    }
    bindSingleton {
        LoginController(
            instance(),
            instance(),
            instance(tag = "jwtIssuer"),
            instance(tag = "jwtAudience"),
            instance(tag = "jwtHMAC256Secret")
        )
    }
    bindSingleton {
        WebauthnIdentityService(
            instance(),
            instance(),
            instance(),
            instance(),
            instance(tag = "webauthnOrigin"),
            instance(tag = "webauthnRpId"),
            instance(tag = "webauthnRpName")
        )
    }

    bindSingleton { UnlockToolAtDeviceViaWebsocket(instance()) }

    bindSingleton { UpdateDeviceFirmwareViaWebsocket(instance()) }

    bindSingleton { RestartDeviceViaWebsocket(instance()) }

    bindSingleton { PrometheusMeterRegistry(PrometheusConfig.DEFAULT) }

    bindEagerSingleton(tag = "gettingConfigurationCounter") {
        MicrometerTaggedCounter<DeviceId>(
            instance(),
            "fabx.device.get_configuration.count",
            "Incremented for every time a device gets its configuration."
        )
    }

    bindEagerSingleton(tag = "toolUsageCounter") {
        MicrometerTaggedCounter<ToolId>(
            instance(),
            "fabx.tool.usage.count",
            "Incremented for every unlock of a tool."
        )
    }

    bindSingleton {
        MetricsController(
            instance(),
            instance(),
            instance(),
            instance(),
            instance()
        )
    }

    bindSingleton {
        WebApp(
            instance(),
            instance(tag = "port"),
            instance(tag = "jwtIssuer"),
            instance(tag = "jwtAudience"),
            instance(tag = "jwtHMAC256Secret"),
            instance(tag = "metricsPassword"),
            instance(tag = "httpsRedirect"),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance()
        )
    }
}