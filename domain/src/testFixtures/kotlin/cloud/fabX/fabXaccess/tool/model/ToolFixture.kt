package cloud.fabX.fabXaccess.tool.model

import cloud.fabX.fabXaccess.common.model.QualificationId
import cloud.fabX.fabXaccess.common.model.ToolId
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFixture

object ToolFixture {

    fun arbitrary(
        toolId: ToolId = ToolIdFixture.arbitrary(),
        aggregateVersion: Long = 1,
        name: String = "tool",
        type: ToolType = ToolType.UNLOCK,
        time: Int = 200,
        idleState: IdleState = IdleState.IDLE_HIGH,
        enabled: Boolean = true,
        wikiLink: String = "https://example.com/tool",
        requiredQualifications: Set<QualificationId> = setOf(QualificationIdFixture.static(42))
    ): Tool = Tool(
        toolId,
        aggregateVersion,
        name,
        type,
        time,
        idleState,
        enabled,
        wikiLink,
        requiredQualifications
    )
}