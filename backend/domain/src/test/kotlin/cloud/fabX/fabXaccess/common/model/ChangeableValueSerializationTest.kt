package cloud.fabX.fabXaccess.common.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import cloud.fabX.fabXaccess.tool.model.ToolType
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.NullSource
import org.junit.jupiter.params.provider.ValueSource

internal class ChangeableValueSerializationTest {

    @Test
    fun `given serialized ChangeToValue of Int when deserializing then deserializes`() {
        // given
        val value: ChangeableValue<Int> = ChangeableValue.ChangeToValueInt(42)
        val encoded = Json.encodeToString(value)

        // when
        val decoded = Json.decodeFromString<ChangeableValue<Int>>(encoded)

        // then
        assertThat(decoded).isEqualTo(value)
    }

    @Test
    fun `given ChangeToValue of String when serializing then serializes`() {
        // given
        val value = ChangeableValue.ChangeToValueString("hello world")

        // when
        val encoded = Json.encodeToString(value)

        // then
        assertThat(encoded).isNotNull()
    }


    @Test
    fun `given serialized ChangeToValue of String when deserializing then deserializes`() {
        // given
        val value: ChangeableValue<String> = ChangeableValue.ChangeToValueString("hello world")
        val encoded = Json.encodeToString(value)

        // when
        val decoded = Json.decodeFromString<ChangeableValue<String>>(encoded)

        // then
        assertThat(decoded).isEqualTo(value)
    }

    @ParameterizedTest
    @ValueSource(strings = ["a string"])
    @NullSource
    fun `given serialized ChangeToValue of optional String when deserializing then deserializes`(s: String?) {
        // given
        val value: ChangeableValue<String?> = ChangeableValue.ChangeToValueOptionalString(s)
        val encoded = Json.encodeToString(value)

        // when
        val decoded = Json.decodeFromString<ChangeableValue<String?>>(encoded)

        // then
        assertThat(decoded).isEqualTo(value)
    }

    @Test
    fun `given serialized ChangeToValue of Boolean when deserializing then deserializes`() {
        // given
        val value: ChangeableValue<Boolean> = ChangeableValue.ChangeToValueBoolean(true)
        val encoded = Json.encodeToString(value)

        // when
        val decoded = Json.decodeFromString<ChangeableValue<Boolean>>(encoded)

        // then
        assertThat(decoded).isEqualTo(value)
    }


    @Test
    fun `given serialized ChangeToValue of ToolType when deserializing then deserializes`() {
        // given
        val value: ChangeableValue<ToolType> = ChangeableValue.ChangeToValueToolType(ToolType.UNLOCK)
        val encoded = Json.encodeToString(value)

        // when
        val decoded = Json.decodeFromString<ChangeableValue<ToolType>>(encoded)

        // then
        assertThat(decoded).isEqualTo(value)
    }
}