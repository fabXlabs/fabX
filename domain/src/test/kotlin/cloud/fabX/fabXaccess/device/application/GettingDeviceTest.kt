package cloud.fabX.fabXaccess.device.application

import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isSameAs
import cloud.fabX.fabXaccess.common.application.ThumbnailCreator
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.ErrorFixture
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.device.model.DeviceFixture
import cloud.fabX.fabXaccess.device.model.DeviceIdFixture
import cloud.fabX.fabXaccess.device.model.DeviceRepository
import cloud.fabX.fabXaccess.user.model.AdminFixture
import isLeft
import isRight
import kotlinx.coroutines.test.runTest
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

    private lateinit var logger: Logger
    private lateinit var deviceRepository: DeviceRepository

    private lateinit var testee: GettingDevice

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock deviceRepository: DeviceRepository
    ) {
        this.logger = logger
        this.deviceRepository = deviceRepository

        testee = GettingDevice({ logger }, deviceRepository)
    }

    @Test
    fun `when getting all then returns all from repository`() = runTest {
        // given
        val device1 = DeviceFixture.arbitrary()
        val device2 = DeviceFixture.arbitrary()
        val device3 = DeviceFixture.arbitrary()

        val devices = setOf(device1, device2, device3)

        whenever(deviceRepository.getAll())
            .thenReturn(devices)

        // when
        val result = testee.getAll(adminActor, correlationId)

        // then
        assertThat(result)
            .isSameAs(devices)
    }

    @Test
    fun `given device exists when getting by id then returns from repository`() = runTest {
        // given
        val device = DeviceFixture.arbitrary(deviceId)

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(device.right())

        // when
        val result = testee.getById(adminActor, correlationId, deviceId)

        // then
        assertThat(result)
            .isRight()
            .isSameAs(device)
    }

    @Test
    fun `given repository error when getting by id then returns error`() = runTest {
        // given
        val expectedError = ErrorFixture.arbitrary()

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(expectedError.left())

        // when
        val result = testee.getById(adminActor, correlationId, deviceId)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(expectedError)
    }

    @Test
    fun `given device exists when getting me then returns from repository`() = runTest {
        // given
        val device = DeviceFixture.arbitrary(deviceId)

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(device.right())

        // when
        val result = testee.getMe(device.asActor(), correlationId)

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(device)
    }

    @Test
    fun `given repository error when getting me then returns error`() = runTest {
        // given
        val device = DeviceFixture.arbitrary(deviceId)

        val expectedError = ErrorFixture.arbitrary()

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(expectedError.left())

        // when
        val result = testee.getMe(device.asActor(), correlationId)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(expectedError)
    }

    @Test
    fun `given device without thumbnail exists when getting thumbnail then returns default thumbnail`() = runTest {
        // given
        val device = DeviceFixture.arbitrary(deviceId)

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(device.right())

        val thumbnailError = Error.DeviceThumbnailNotFound("No thumbnail for Device with id $deviceId found.", deviceId)

        whenever(deviceRepository.getThumbnail(deviceId))
            .thenReturn(thumbnailError.left())

        // when
        val result = testee.getThumbnail(adminActor, correlationId, deviceId)

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(ThumbnailCreator.default)
    }

    @Test
    fun `given device with thumbnail exists when getting thumbnail then returns thumbnail`() = runTest {
        // given
        val device = DeviceFixture.arbitrary(deviceId)

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(device.right())

        val thumbnailData = ByteArray(42) { it.toByte() }

        whenever(deviceRepository.getThumbnail(deviceId))
            .thenReturn(thumbnailData.right())

        // when
        val result = testee.getThumbnail(adminActor, correlationId, deviceId)

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(thumbnailData)
    }

    @Test
    fun `given repository error when getting thumbnail then returns error`() = runTest {
        // given
        val expectedError = ErrorFixture.arbitrary()

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(expectedError.left())

        // when
        val result = testee.getThumbnail(adminActor, correlationId, deviceId)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(expectedError)
    }
}