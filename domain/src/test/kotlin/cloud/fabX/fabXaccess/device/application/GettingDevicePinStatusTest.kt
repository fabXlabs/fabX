package cloud.fabX.fabXaccess.device.application

import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.ErrorFixture
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.model.SystemActor
import cloud.fabX.fabXaccess.device.model.DeviceIdFixture
import cloud.fabX.fabXaccess.device.model.DevicePinStatus
import cloud.fabX.fabXaccess.device.model.DevicePinStatusRepository
import cloud.fabX.fabXaccess.user.model.UserFixture
import isLeft
import isRight
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.whenever

@MockitoSettings
internal class GettingDevicePinStatusTest {
    private val memberActor = UserFixture.arbitrary().asMember()
    private val correlationId = CorrelationIdFixture.arbitrary()

    private lateinit var logger: Logger
    private lateinit var devicePinStatusRepository: DevicePinStatusRepository

    private lateinit var testee: GettingDevicePinStatus

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock devicePinStatusRepository: DevicePinStatusRepository
    ) {
        this.logger = logger
        this.devicePinStatusRepository = devicePinStatusRepository

        testee = GettingDevicePinStatus({ logger }, devicePinStatusRepository)
    }

    @Test
    fun `when getting device pin status then returns device pin status`() = runTest {
        // given
        val data = setOf(
            DevicePinStatus(
                DeviceIdFixture.arbitrary(),
                mapOf(1 to true, 2 to false)
            ),
            DevicePinStatus(
                DeviceIdFixture.arbitrary(),
                mapOf(1 to false, 2 to true)
            )
        )

        whenever(devicePinStatusRepository.getAll())
            .thenReturn(data)

        // when
        val result = testee.getAll(SystemActor, correlationId)

        // then
        assertThat(result)
            .isEqualTo(data)
    }

    @Test
    fun `when getting device pin status as member then returns device pin status`() = runTest {
        // given
        val data = setOf(
            DevicePinStatus(
                DeviceIdFixture.arbitrary(),
                mapOf(1 to true, 2 to false)
            ),
            DevicePinStatus(
                DeviceIdFixture.arbitrary(),
                mapOf(1 to false, 2 to true)
            )
        )

        whenever(devicePinStatusRepository.getAll())
            .thenReturn(data)

        // when
        val result = testee.getAll(memberActor, correlationId)

        // then
        assertThat(result)
            .isEqualTo(data)
    }

    @Test
    fun `given known pin status when getting by id then returns from device pin status`() = runTest {
        // given
        val deviceId = DeviceIdFixture.arbitrary()

        val devicePinStatus = DevicePinStatus(
            deviceId,
            mapOf(1 to false, 2 to true)
        )

        whenever { devicePinStatusRepository.getById(deviceId) }
            .thenReturn(devicePinStatus.right())

        // when
        val result = testee.getById(memberActor, correlationId, deviceId)

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(devicePinStatus)
    }

    @Test
    fun `given unknown pin status when getting by id then returns error`() = runTest {
        // given
        val deviceId = DeviceIdFixture.arbitrary()

        val error = ErrorFixture.arbitrary()

        whenever { devicePinStatusRepository.getById(deviceId) }
            .thenReturn(error.left())

        // when
        val result = testee.getById(memberActor, correlationId, deviceId)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }
}
