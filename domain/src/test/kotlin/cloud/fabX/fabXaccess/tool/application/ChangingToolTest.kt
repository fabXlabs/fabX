package cloud.fabX.fabXaccess.tool.application

import arrow.core.None
import arrow.core.left
import arrow.core.right
import arrow.core.some
import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.ErrorFixture
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.tool.model.IdleState
import cloud.fabX.fabXaccess.tool.model.ToolDetailsChanged
import cloud.fabX.fabXaccess.tool.model.ToolFixture
import cloud.fabX.fabXaccess.tool.model.ToolIdFixture
import cloud.fabX.fabXaccess.tool.model.ToolRepository
import cloud.fabX.fabXaccess.tool.model.ToolType
import cloud.fabX.fabXaccess.user.model.AdminFixture
import isNone
import isSome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.whenever

@MockitoSettings
internal class ChangingToolTest {

    private val adminActor = AdminFixture.arbitrary()
    private val correlationId = CorrelationIdFixture.arbitrary()

    private val toolId = ToolIdFixture.arbitrary()

    private var logger: Logger? = null
    private var toolRepository: ToolRepository? = null

    private var testee: ChangingTool? = null

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock toolRepository: ToolRepository
    ) {
        this.logger = logger
        this.toolRepository = toolRepository
        DomainModule.configureLoggerFactory { logger }
        DomainModule.configureToolRepository(toolRepository)

        testee = ChangingTool()
    }

    @Test
    fun `given tool can be found when changing tool details then sourcing event is created and stored`() {
        // given
        val tool = ToolFixture.arbitrary(toolId, aggregateVersion = 1)

        val newName = ChangeableValue.ChangeToValue("newName")
        val newType = ChangeableValue.ChangeToValue(ToolType.UNLOCK)
        val newTime = ChangeableValue.ChangeToValue(42)
        val newIdleState = ChangeableValue.ChangeToValue(IdleState.IDLE_LOW)
        val newEnabled = ChangeableValue.ChangeToValue(false)
        val newWikiLink = ChangeableValue.LeaveAsIs
        val newRequiredQualifications = ChangeableValue.LeaveAsIs

        val expectedSourcingEvent = ToolDetailsChanged(
            toolId,
            2,
            adminActor.id,
            correlationId,
            newName,
            newType,
            newTime,
            newIdleState,
            newEnabled,
            newWikiLink,
            newRequiredQualifications
        )

        whenever(toolRepository!!.getById(toolId))
            .thenReturn(tool.right())

        whenever(toolRepository!!.store(expectedSourcingEvent))
            .thenReturn(None)

        // when
        val result = testee!!.changeToolDetails(
            adminActor,
            correlationId,
            toolId,
            newName,
            newType,
            newTime,
            newIdleState,
            newEnabled,
            newWikiLink,
            newRequiredQualifications
        )

        // then
        assertThat(result).isNone()

        val inOrder = inOrder(toolRepository!!)
        inOrder.verify(toolRepository!!).getById(toolId)
        inOrder.verify(toolRepository!!).store(expectedSourcingEvent)
    }

    @Test
    fun `given tool cannot be found when changing tool details then returns error`() {
        // given
        val error = ErrorFixture.arbitrary()

        whenever(toolRepository!!.getById(toolId))
            .thenReturn(error.left())

        // when
        val result = testee!!.changeToolDetails(
            adminActor,
            correlationId,
            toolId,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs
        )

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(error)
    }

    @Test
    fun `given sourcing event cannot be stored when changing tool details then returns error`() {
        // given
        val tool = ToolFixture.arbitrary(toolId, aggregateVersion = 1)

        val event = ToolDetailsChanged(
            toolId,
            2,
            adminActor.id,
            correlationId,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs
        )

        val error = ErrorFixture.arbitrary()

        whenever(toolRepository!!.getById(toolId))
            .thenReturn(tool.right())

        whenever(toolRepository!!.store(event))
            .thenReturn(error.some())

        // when
        val result = testee!!.changeToolDetails(
            adminActor,
            correlationId,
            toolId,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs
        )

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(error)
    }
}