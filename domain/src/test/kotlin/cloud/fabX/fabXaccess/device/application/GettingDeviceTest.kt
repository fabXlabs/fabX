package cloud.fabX.fabXaccess.device.application

import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isSameAs
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.ErrorFixture
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.device.model.DeviceFixture
import cloud.fabX.fabXaccess.device.model.DeviceIdFixture
import cloud.fabX.fabXaccess.device.model.DeviceRepository
import cloud.fabX.fabXaccess.user.model.AdminFixture
import isLeft
import isRight
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.whenever

@MockitoSettings
internal class GettingDeviceTest {

    private val adminActor = AdminFixture.arbitrary()
    private val deviceId = DeviceIdFixture.arbitrary()
    private val correlationId = CorrelationIdFixture.arbitrary()

    private var logger: Logger? = null
    private var deviceRepository: DeviceRepository? = null

    private var testee: GettingDevice? = null

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock deviceRepository: DeviceRepository
    ) {
        this.logger = logger
        this.deviceRepository = deviceRepository

        DomainModule.configureLoggerFactory { logger }
        DomainModule.configureDeviceRepository(deviceRepository)

        testee = GettingDevice()
    }

    @Test
    fun `when getting all then returns all from repository`() {
        // given
        val device1 = DeviceFixture.arbitrary()
        val device2 = DeviceFixture.arbitrary()
        val device3 = DeviceFixture.arbitrary()

        val devices = setOf(device1, device2, device3)

        whenever(deviceRepository!!.getAll())
            .thenReturn(devices)

        // when
        val result = testee!!.getAll(adminActor, correlationId)

        // then
        assertThat(result)
            .isSameAs(devices)
    }

    @Test
    fun `given device exists when getting by id then returns from repository`() {
        // given
        val device = DeviceFixture.arbitrary(deviceId)

        whenever(deviceRepository!!.getById(deviceId))
            .thenReturn(device.right())

        // when
        val result = testee!!.getById(adminActor, correlationId, deviceId)

        // then
        assertThat(result)
            .isRight()
            .isSameAs(device)
    }

    @Test
    fun `given repository error when getting by id then returns error`() {
        // given
        val expectedError = ErrorFixture.arbitrary()

        whenever(deviceRepository!!.getById(deviceId))
            .thenReturn(expectedError.left())

        // when
        val result = testee!!.getById(adminActor, correlationId, deviceId)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(expectedError)
    }
}