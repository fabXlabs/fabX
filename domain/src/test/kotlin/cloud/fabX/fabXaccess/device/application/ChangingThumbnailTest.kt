package cloud.fabX.fabXaccess.device.application

import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.device.model.DeviceFixture
import cloud.fabX.fabXaccess.device.model.DeviceIdFixture
import cloud.fabX.fabXaccess.device.model.DeviceRepository
import cloud.fabX.fabXaccess.user.model.AdminFixture
import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.JpegWriter
import isNone
import isSome
import java.awt.Color
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.whenever

@MockitoSettings
internal class ChangingThumbnailTest {
    private val adminActor = AdminFixture.arbitrary()
    private val correlationId = CorrelationIdFixture.arbitrary()

    private val deviceId = DeviceIdFixture.arbitrary()

    private val thumbnailData = ImmutableImage.create(600, 600)
        .fill(Color.DARK_GRAY)
        .bytes(JpegWriter.Default)

    private lateinit var logger: Logger
    private lateinit var deviceRepository: DeviceRepository

    private lateinit var testee: ChangingThumbnail

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock deviceRepository: DeviceRepository
    ) {
        this.logger = logger
        this.deviceRepository = deviceRepository

        testee = ChangingThumbnail({ logger }, deviceRepository)
    }

    @Test
    fun `given device can be found when changing thumbnail then thumbnail is stored`() = runTest {
        // given
        val device = DeviceFixture.arbitrary(deviceId)

        val thumbnailData = ImmutableImage.create(600, 600)
            .fill(Color.LIGHT_GRAY)
            .bytes(JpegWriter.Default)

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(device.right())

        whenever(deviceRepository.storeThumbnail(deviceId, adminActor.id, thumbnailData))
            .thenReturn(Unit.right())

        // when
        val result = testee.changeDeviceThumbnail(adminActor, correlationId, deviceId, thumbnailData)

        // then
        assertThat(result).isNone()
    }

    @Test
    fun `given device cannot be found when changing thumbnail then returns error`() = runTest {
        // given
        val error = Error.DeviceNotFound("message", deviceId)

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(error.left())

        // when
        val result = testee.changeDeviceThumbnail(adminActor, correlationId, deviceId, thumbnailData)

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(error)
    }

    @Test
    fun `given invalid thumbnail when changing thumbnail then returns error`() = runTest {
        // given
        val invalidThumbnailData = ByteArray(10) { it.toByte() }

        val device = DeviceFixture.arbitrary(deviceId)

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(device.right())

        // when
        val result = testee.changeDeviceThumbnail(adminActor, correlationId, deviceId, invalidThumbnailData)

        // then
        assertThat(result)
            .isSome()
            .isInstanceOf(Error.ThumbnailInvalid::class)
    }

    @Test
    fun `given thumbnail cannot be stored when changing thumbnail then returns error`() = runTest {
        // given
        val device = DeviceFixture.arbitrary(deviceId)

        val thumbnailData = ImmutableImage.create(600, 600)
            .fill(Color.LIGHT_GRAY)
            .bytes(JpegWriter.Default)

        val expectedError = Error.DeviceNotFound("some message", deviceId)

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(device.right())

        whenever(deviceRepository.storeThumbnail(deviceId, adminActor.id, thumbnailData))
            .thenReturn(expectedError.left())

        // when
        val result = testee.changeDeviceThumbnail(adminActor, correlationId, deviceId, thumbnailData)

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(expectedError)
    }
}