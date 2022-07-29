package cloud.fabX.fabXaccess.common.model

import assertk.assertThat
import assertk.assertions.isNotNull
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test

// TODO write tests
internal class ChangeableValueSerializationTest {

    @Test
    fun `given ChangeToValue of String when serializing then serializes`() {
        // given
        val value = ChangeableValue.ChangeToValue("hello world")

        // when
        val encoded = Json.encodeToString(value)

        // then
        println(encoded)
        assertThat(encoded).isNotNull()
    }

    @Test
    fun `given serialized ChangeToValue of String when deserializing then deserializes`() {
        TODO()
    }

    // TODO ChangeableValue of Int
    // TODO ChangeableValue of Long
    // TODO ChangeableValue of an Enum
}