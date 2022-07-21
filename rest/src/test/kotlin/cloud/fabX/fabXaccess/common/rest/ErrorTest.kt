package cloud.fabX.fabXaccess.common.rest

import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import org.junit.jupiter.api.Test

internal class ErrorTest {
    @Test
    fun `when mapping domain model to rest model then returns mapped`() {
        // given
        val message = "msg123"
        val userId = UserIdFixture.arbitrary()
        val error = Error.UserNotFound(message, userId)

        val expectedResult = Error(
            message,
            mapOf("userId" to userId.serialize())
        )

        // when
        val result = error.toRestModel()

        // then
        assertThat(result).isEqualTo(expectedResult)
    }
}