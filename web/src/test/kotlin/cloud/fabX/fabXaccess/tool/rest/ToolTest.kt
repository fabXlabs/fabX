package cloud.fabX.fabXaccess.tool.rest

import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFixture
import cloud.fabX.fabXaccess.tool.model.IdleState
import cloud.fabX.fabXaccess.tool.model.ToolFixture
import cloud.fabX.fabXaccess.tool.model.ToolIdFixture
import cloud.fabX.fabXaccess.tool.model.ToolType
import org.junit.jupiter.api.Test

internal class ToolTest {

    @Test
    fun `when mapping domain model to rest model then returns mapped`() {
        // given
        val qualificationId1 = QualificationIdFixture.arbitrary()
        val qualificationId2 = QualificationIdFixture.arbitrary()
        val qualificationId3 = QualificationIdFixture.arbitrary()

        val toolId = ToolIdFixture.arbitrary()
        val tool = ToolFixture.arbitrary(
            toolId,
            34,
            "tool1",
            ToolType.KEEP,
            false,
            789,
            IdleState.IDLE_HIGH,
            true,
            null,
            "https://example.com/tool1",
            setOf(qualificationId1, qualificationId2, qualificationId3)
        )

        val expectedResult = Tool(
            toolId.serialize(),
            34,
            "tool1",
            cloud.fabX.fabXaccess.tool.rest.ToolType.KEEP,
            false,
            789,
            cloud.fabX.fabXaccess.tool.rest.IdleState.IDLE_HIGH,
            true,
            null,
            "https://example.com/tool1",
            setOf(qualificationId1.serialize(), qualificationId2.serialize(), qualificationId3.serialize())
        )

        // when
        val result = tool.toRestModel()

        // then
        assertThat(result).isEqualTo(expectedResult)
    }
}