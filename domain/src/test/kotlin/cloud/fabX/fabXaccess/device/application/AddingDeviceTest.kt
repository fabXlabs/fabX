package cloud.fabX.fabXaccess.device.application

import arrow.core.None
import arrow.core.some
import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.model.ErrorFixture
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.device.model.DeviceCreated
import cloud.fabX.fabXaccess.device.model.DeviceIdFixture
import cloud.fabX.fabXaccess.device.model.DeviceRepository
import cloud.fabX.fabXaccess.user.model.AdminFixture
import isNone
import isSome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.whenever

@MockitoSettings
internal class AddingDeviceTest {

    private val adminActor = AdminFixture.arbitraryAdmin()

    private val deviceId = DeviceIdFixture.arbitrary()

    private var logger: Logger? = null
    private var deviceRepository: DeviceRepository? = null

    private var testee: AddingDevice? = null

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock deviceRepository: DeviceRepository
    ) {
        this.logger = logger
        this.deviceRepository = deviceRepository

        DomainModule.configureLoggerFactory { logger }
        DomainModule.configureDeviceIdFactory { deviceId }
        DomainModule.configureDeviceRepository(deviceRepository)

        testee = AddingDevice()
    }

    @Test
    fun `given valid values when adding device then sourcing event is created and stored`() {
        // given
        val name = "name"
        val backgroundUrl = "https://example.com/bg.bmp"
        val backupBackendUrl = "https://backup.example.com"

        val expectedSourcingEvent = DeviceCreated(
            deviceId,
            adminActor.id,
            name,
            backgroundUrl,
            backupBackendUrl
        )

        whenever(deviceRepository!!.store(expectedSourcingEvent))
            .thenReturn(None)

        // when
        val result = testee!!.addDevice(
            adminActor,
            name,
            backgroundUrl,
            backupBackendUrl
        )

        // then
        assertThat(result).isNone()
    }

    @Test
    fun `given sourcing event cannot be stored when adding device then returns error`() {
        // given
        val name = "name"
        val backgroundUrl = "https://example.com/bg.bmp"
        val backupBackendUrl = "https://backup.example.com"

        val event = DeviceCreated(
            deviceId,
            adminActor.id,
            name,
            backgroundUrl,
            backupBackendUrl
        )

        val error = ErrorFixture.arbitrary()

        whenever(deviceRepository!!.store(event))
            .thenReturn(error.some())

        // when
        val result = testee!!.addDevice(
            adminActor,
            name,
            backgroundUrl,
            backupBackendUrl
        )

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(error)
    }
}