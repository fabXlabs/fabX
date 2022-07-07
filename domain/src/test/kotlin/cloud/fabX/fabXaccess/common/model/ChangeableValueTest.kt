package cloud.fabX.fabXaccess.common.model

import assertk.assertThat
import assertk.assertions.isTrue
import kotlin.test.Test

internal class ChangeableValueTest {
    @Test
    fun `when creating ChangeToValue then it pattern matches correctly`() {
        // when
        val result: ChangeableValue<String> = ChangeableValue.ChangeToValue("new value")

        // then
        when (result) {
            is ChangeableValue.ChangeToValue -> println("change to value: ${result.value}")
            ChangeableValue.LeaveAsIs -> println("leave as is")
        }

        assertThat(
            when (result) {
                is ChangeableValue.ChangeToValue -> true
                ChangeableValue.LeaveAsIs -> false
            }
        )
            .isTrue()
    }

    @Test
    fun `when creating LeaveAsIs then it pattern matches correctly`() {
        // when
        val result: ChangeableValue<String> = ChangeableValue.LeaveAsIs

        // then
        assertThat(
            when (result) {
                is ChangeableValue.ChangeToValue -> false
                ChangeableValue.LeaveAsIs -> true
            }
        )
            .isTrue()
    }

    @Test
    fun `given nullable type and null when creating ChangeToValue then it pattern matches correctly`() {
        // when
        val result: ChangeableValue<String?> = ChangeableValue.ChangeToValue(null)

        // then
        assertThat(
            when (result) {
                is ChangeableValue.ChangeToValue -> true
                ChangeableValue.LeaveAsIs -> false
            }
        )
            .isTrue()
    }

    @Test
    fun `given nullable type and value when creating ChangeToValue then it pattern matches correctly`() {
        // when
        val result: ChangeableValue<String?> = ChangeableValue.ChangeToValue("new value")

        // then
        assertThat(
            when (result) {
                is ChangeableValue.ChangeToValue -> true
                ChangeableValue.LeaveAsIs -> false
            }
        )
            .isTrue()
    }

}