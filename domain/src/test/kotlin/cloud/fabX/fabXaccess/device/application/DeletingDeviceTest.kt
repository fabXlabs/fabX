package cloud.fabX.fabXaccess.device.application

import arrow.core.None
import arrow.core.left
import arrow.core.right
import arrow.core.some
import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.ErrorFixture
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.device.model.DeviceDeleted
import cloud.fabX.fabXaccess.device.model.DeviceFixture
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
internal class DeletingDeviceTest {

    private val adminActor = AdminFixture.arbitraryAdmin()

    private val deviceId = DeviceIdFixture.arbitraryId()

    private var logger: Logger? = null
    private var deviceRepository: DeviceRepository? = null

    private var testee: DeletingDevice? = null

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock deviceRepository: DeviceRepository
    ) {
        this.logger = logger
        this.deviceRepository = deviceRepository
        DomainModule.configureLoggerFactory { logger }
        DomainModule.configureDeviceRepository(deviceRepository)

        testee = DeletingDevice()
    }

    @Test
    fun `given device can be found when deleting device then sourcing event is created and stored`() {
        // given
        val device = DeviceFixture.arbitraryDevice(deviceId, aggregateVersion = 42)

        val expectedSourcingEvent = DeviceDeleted(
            deviceId,
            43,
            adminActor.id
        )

        whenever(deviceRepository!!.getById(deviceId))
            .thenReturn(device.right())

        whenever(deviceRepository!!.store(expectedSourcingEvent))
            .thenReturn(None)

        // when
        val result = testee!!.deleteDevice(
            adminActor,
            deviceId
        )

        // then
        assertThat(result).isNone()
    }

    @Test
    fun `given device cannot be found when deleting device then returns error`() {
        // given
        val error = Error.DeviceNotFound("message", deviceId)

        whenever(deviceRepository!!.getById(deviceId))
            .thenReturn(error.left())

        // when
        val result = testee!!.deleteDevice(
            adminActor,
            deviceId
        )

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(error)
    }

    @Test
    fun `given sourcing event cannot be stored when deleting device then returns error`() {
        // given
        val device = DeviceFixture.arbitraryDevice(deviceId, aggregateVersion = 1)

        val event = DeviceDeleted(
            deviceId,
            2,
            adminActor.id
        )

        val error = ErrorFixture.arbitrary()

        whenever(deviceRepository!!.getById(deviceId))
            .thenReturn(device.right())

        whenever(deviceRepository!!.store(event))
            .thenReturn(error.some())

        // when
        val result = testee!!.deleteDevice(
            adminActor,
            deviceId
        )

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(error)
    }
}