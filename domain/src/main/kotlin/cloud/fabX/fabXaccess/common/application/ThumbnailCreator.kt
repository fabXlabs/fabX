package cloud.fabX.fabXaccess.common.application

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import cloud.fabX.fabXaccess.common.model.CorrelationId
import cloud.fabX.fabXaccess.common.model.Error
import com.sksamuel.scrimage.ImageParseException
import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.JpegWriter
import java.awt.Color

object ThumbnailCreator {
    private val writer = JpegWriter.Default

    val default: ByteArray = ImmutableImage.create(600, 600)
        .fill(Color.LIGHT_GRAY)
        .bytes(writer)

    fun create(
        image: ByteArray,
        correlationId: CorrelationId
    ): Either<Error, ByteArray> {
        return try {
            ImmutableImage.loader()
                .fromBytes(image)
                .cover(600, 600)
                .bytes(writer)
                .right()
        } catch (e: ImageParseException) {
            Error.ThumbnailInvalid(
                "Invalid Thumbnail: ${e.message}",
                correlationId
            ).left()
        }
    }
}