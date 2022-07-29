package cloud.fabX.fabXaccess.common.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test

// TODO write tests
internal class ChangeableValueSerializationTest {

    @Test
    fun `generic type get test`() {
        // given
        val l: List<ChangeableValue<Any>> = listOf(
            ChangeableValue.ChangeToValueString("hello world"),
            ChangeableValue.ChangeToValueInt(42),
        )

        // when
        l.forEach {
            when (it) {
                is ChangeableValue.ChangeToValue<*> -> {
                    when (it.value) {
                        is String -> println("is string")
                        is Int -> println("is int")
                        else -> println("is other type")
                    }
                }
                ChangeableValue.LeaveAsIs -> println("is leave as is")
            }
        }


        // then

    }

    @Test
    fun `given ChangeToValue of String when serializing then serializes`() {
        // given
        val value = ChangeableValue.ChangeToValueString("hello world")

        // when
        val encoded = Json.encodeToString(value)

        // then
        println(encoded)
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

    // TODO ChangeableValue of Int
    // TODO ChangeableValue of Long
    // TODO ChangeableValue of an Enum
}