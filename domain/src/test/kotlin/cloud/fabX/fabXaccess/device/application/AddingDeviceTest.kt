package cloud.fabX.fabXaccess.device.application

import FixedClock
import arrow.core.None
import arrow.core.some
import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.ErrorFixture
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.device.model.DeviceCreated
import cloud.fabX.fabXaccess.device.model.DeviceIdFixture
import cloud.fabX.fabXaccess.device.model.DeviceRepository
import cloud.fabX.fabXaccess.user.model.AdminFixture
import isLeft
import isRight
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@MockitoSettings
internal class AddingDeviceTest {

    private val adminActor = AdminFixture.arbitrary()
    private val correlationId = CorrelationIdFixture.arbitrary()

    private val deviceId = DeviceIdFixture.arbitrary()

    private val fixedInstant = Clock.System.now()
    private val fixedClock = FixedClock(fixedInstant)

    private lateinit var logger: Logger
    private lateinit var deviceRepository: DeviceRepository

    private lateinit var testee: AddingDevice

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock deviceRepository: DeviceRepository
    ) {
        this.logger = logger
        this.deviceRepository = deviceRepository

        testee = AddingDevice({ logger }, deviceRepository, { deviceId }, fixedClock)
    }

    @Test
    fun `given valid values when adding device then sourcing event is created and stored`() = runTest {
        // given
        val name = "name"
        val backgroundUrl = "https://example.com/bg.bmp"
        val backupBackendUrl = "https://backup.example.com"
        val mac = "A0B1C2D3E4F5"
        val secret = "755f78e6a43a7d319e5a05b4a4eaa800"

        val expectedSourcingEvent = DeviceCreated(
            deviceId,
            adminActor.id,
            fixedInstant,
            correlationId,
            name,
            backgroundUrl,
            backupBackendUrl,
            mac,
            secret
        )

        whenever(deviceRepository.store(expectedSourcingEvent))
            .thenReturn(None)

        // when
        val result = testee.addDevice(
            adminActor,
            correlationId,
            name,
            backgroundUrl,
            backupBackendUrl,
            mac,
            secret
        )

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(deviceId)
    }

    @Test
    fun `given sourcing event cannot be stored when adding device then returns error`() = runTest {
        // given
        val name = "name"
        val backgroundUrl = "https://example.com/bg.bmp"
        val backupBackendUrl = "https://backup.example.com"
        val mac = "AA11BB22CC33"
        val secret = "9d2fedd835842a422ddee01215d0495e"

        val event = DeviceCreated(
            deviceId,
            adminActor.id,
            fixedInstant,
            correlationId,
            name,
            backgroundUrl,
            backupBackendUrl,
            mac,
            secret
        )

        val error = ErrorFixture.arbitrary()

        whenever(deviceRepository.store(event))
            .thenReturn(error.some())

        // when
        val result = testee.addDevice(
            adminActor,
            correlationId,
            name,
            backgroundUrl,
            backupBackendUrl,
            mac,
            secret
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }
}