package cloud.fabX.fabXaccess.device.application

import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsOnly
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.ErrorFixture
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.device.model.DeviceFixture
import cloud.fabX.fabXaccess.device.model.DeviceIdFixture
import cloud.fabX.fabXaccess.device.model.DeviceRepository
import cloud.fabX.fabXaccess.device.model.GetDeviceConnectionStatus
import cloud.fabX.fabXaccess.user.model.UserFixture
import isLeft
import isRight
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.whenever

@MockitoSettings
internal class GettingDeviceConnectionStatusTest {

    private val memberActor = UserFixture.arbitrary().asMember()
    private val deviceId = DeviceIdFixture.arbitrary()
    private val correlationId = CorrelationIdFixture.arbitrary()

    private lateinit var logger: Logger
    private lateinit var deviceRepository: DeviceRepository
    private lateinit var getDeviceConnectionStatus: GetDeviceConnectionStatus

    private lateinit var testee: GettingDeviceConnectionStatus

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock deviceRepository: DeviceRepository,
        @Mock getDeviceConnectionStatus: GetDeviceConnectionStatus
    ) {
        this.logger = logger
        this.deviceRepository = deviceRepository
        this.getDeviceConnectionStatus = getDeviceConnectionStatus

        testee = GettingDeviceConnectionStatus({ logger }, deviceRepository, getDeviceConnectionStatus)
    }

    @Test
    fun `when getting all then returns all from repository with associated connection status`() = runTest {
        // given
        val device1 = DeviceFixture.arbitrary()
        val device2 = DeviceFixture.arbitrary()
        val device3 = DeviceFixture.arbitrary()

        val devices = setOf(device1, device2, device3)

        whenever(deviceRepository.getAll())
            .thenReturn(devices)

        whenever(getDeviceConnectionStatus.isConnected(device1.id))
            .thenReturn(true)

        whenever(getDeviceConnectionStatus.isConnected(device2.id))
            .thenReturn(false)

        whenever(getDeviceConnectionStatus.isConnected(device3.id))
            .thenReturn(true)

        // when
        val result = testee.getAll(memberActor, correlationId)

        // then
        assertThat(result)
            .containsOnly(
                device1.id to true,
                device2.id to false,
                device3.id to true,
            )
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `given device exists when getting by id then returns from repository with associated connection status`(
        connectionStatus: Boolean
    ) =
        runTest {
            // given
            val device = DeviceFixture.arbitrary(deviceId)

            whenever(deviceRepository.getById(deviceId))
                .thenReturn(device.right())

            whenever(getDeviceConnectionStatus.isConnected(deviceId))
                .thenReturn(connectionStatus)

            // when
            val result = testee.getById(memberActor, correlationId, deviceId)

            // then
            assertThat(result)
                .isRight()
                .isEqualTo(connectionStatus)
        }

    @Test
    fun `given repository error when getting by id then returns error from the getDeviceConnectionStatus`() = runTest {
        // given
        val expectedError = ErrorFixture.arbitrary()

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(expectedError.left())

        // when
        val result = testee.getById(memberActor, correlationId, deviceId)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(expectedError)
    }
}
