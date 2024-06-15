package cloud.fabX.fabXaccess.tool.application

import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.tool.model.ToolFixture
import cloud.fabX.fabXaccess.tool.model.ToolIdFixture
import cloud.fabX.fabXaccess.tool.model.ToolRepository
import cloud.fabX.fabXaccess.user.model.AdminFixture
import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.JpegWriter
import isLeft
import isRight
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

    private val toolId = ToolIdFixture.arbitrary()

    private val thumbnailData = ImmutableImage.create(600, 600)
        .fill(Color.DARK_GRAY)
        .bytes(JpegWriter.Default)

    private lateinit var logger: Logger
    private lateinit var toolRepository: ToolRepository

    private lateinit var testee: ChangingThumbnail

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock toolRepository: ToolRepository
    ) {
        this.logger = logger
        this.toolRepository = toolRepository

        testee = ChangingThumbnail({ logger }, toolRepository)
    }

    @Test
    fun `given tool can be found when changing thumbnail then thumbnail is stored`() = runTest {
        // given
        val tool = ToolFixture.arbitrary(toolId)

        val thumbnailData = ImmutableImage.create(600, 600)
            .fill(Color.LIGHT_GRAY)
            .bytes(JpegWriter.Default)

        whenever(toolRepository.getById(toolId))
            .thenReturn(tool.right())

        whenever(toolRepository.storeThumbnail(toolId, adminActor.id, thumbnailData))
            .thenReturn(Unit.right())

        // when
        val result = testee.changeToolThumbnail(adminActor, correlationId, toolId, thumbnailData)

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(Unit)
    }

    @Test
    fun `given tool cannot be found when changing thumbnail then returns error`() = runTest {
        // given
        val error = Error.ToolNotFound("message", toolId)

        whenever(toolRepository.getById(toolId))
            .thenReturn(error.left())

        // when
        val result = testee.changeToolThumbnail(adminActor, correlationId, toolId, thumbnailData)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }

    @Test
    fun `given invalid thumbnail when changing thumbnail then returns error`() = runTest {
        // given
        val invalidThumbnailData = ByteArray(10) { it.toByte() }

        val tool = ToolFixture.arbitrary(toolId)

        whenever(toolRepository.getById(toolId))
            .thenReturn(tool.right())

        // when
        val result = testee.changeToolThumbnail(adminActor, correlationId, toolId, invalidThumbnailData)

        // then
        assertThat(result)
            .isLeft()
            .isInstanceOf(Error.ThumbnailInvalid::class)
    }

    @Test
    fun `given thumbnail cannot be stored when changing thumbnail then returns error`() = runTest {
        // given
        val tool = ToolFixture.arbitrary(toolId)

        val thumbnailData = ImmutableImage.create(600, 600)
            .fill(Color.LIGHT_GRAY)
            .bytes(JpegWriter.Default)

        val expectedError = Error.ToolNotFound("some message", toolId)

        whenever(toolRepository.getById(toolId))
            .thenReturn(tool.right())

        whenever(toolRepository.storeThumbnail(toolId, adminActor.id, thumbnailData))
            .thenReturn(expectedError.left())

        // when
        val result = testee.changeToolThumbnail(adminActor, correlationId, toolId, thumbnailData)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(expectedError)
    }
}