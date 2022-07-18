package cloud.fabX.fabXaccess.common.model

import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import isLeft
import isNone
import isRight
import isSome
import org.junit.jupiter.api.Test

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

    @Test
    fun `given ChangeToValue when getting as Option then returns Some`() {
        // given
        val changeable = ChangeableValue.ChangeToValue("value")

        // when
        val result = changeable.asOption()

        // then
        assertThat(result)
            .isSome()
            .isEqualTo("value")
    }

    @Test
    fun `given LeaveAsIs when getting as Option then returns None`() {
        // given
        val changeable = ChangeableValue.LeaveAsIs

        // when
        val result = changeable.asOption()

        // then
        assertThat(result).isNone()
    }

    @Test
    fun `given ChangeToValue when bimap then returns mapped value`() {
        // given
        val changeable = ChangeableValue.ChangeToValue("value")

        // when
        val result = changeable.bimap(
            { throw IllegalArgumentException("Not expected to be called") },
            {
                if (it != "value") {
                    throw IllegalArgumentException("Expected other argument")
                } else {
                    "mapped"
                }
            }
        )

        // then
        assertThat(result)
            .isRight()
            .isEqualTo("mapped")
    }

    @Test
    fun `given LeaveAsIs when bimap then returns mapped value`() {
        // given
        val changeable = ChangeableValue.LeaveAsIs

        // when
        val result = changeable.bimap(
            { "mapped" },
            { throw IllegalArgumentException("Not expected to be called") }
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo("mapped")
    }

    @Test
    fun `given ChangeToValue when biFlatmap then returns mapped value`() {
        // given
        val changeable = ChangeableValue.ChangeToValue("value")

        // when
        val result = changeable.biFlatmap(
            { throw IllegalArgumentException("Not expected to be called") },
            {
                if (it != "value") {
                    throw IllegalArgumentException("Expected other argument")
                } else {
                    "mapped".right()
                }
            }
        )

        // then
        assertThat(result)
            .isRight()
            .isEqualTo("mapped")
    }

    @Test
    fun `given LeaveAsIs when biFlatmap then returns mapped value`() {
        // given
        val changeable = ChangeableValue.LeaveAsIs

        // when
        val result = changeable.biFlatmap(
            { "mapped".right() },
            { throw IllegalArgumentException("Not expected to be called") }
        )

        // then
        assertThat(result)
            .isRight()
            .isEqualTo("mapped")
    }
}