package cloud.fabX.fabXaccess.common.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import org.junit.jupiter.api.Test

internal class DeviceIdTest {
    @Test
    fun `given id when checking equals to same id then returns true`() {
        // given
        val id = newDeviceId()
        val sameId = DeviceId.fromString(id.serialize())

        // when
        val result = (id == sameId)

        // then
        assertThat(result).isTrue()
    }

    @Test
    fun `given identical ids when getting hash then is identical`() {
        // given
        val id = newDeviceId()
        val sameId = DeviceId.fromString(id.serialize())

        // when
        val hash1 = id.hashCode()
        val hash2 = sameId.hashCode()

        // then
        assertThat(hash1).isEqualTo(hash2)
    }
}