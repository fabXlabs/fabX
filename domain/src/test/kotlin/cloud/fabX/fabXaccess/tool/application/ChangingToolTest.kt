package cloud.fabX.fabXaccess.tool.application

import FixedClock
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
import cloud.fabX.fabXaccess.qualification.model.GettingQualificationById
import cloud.fabX.fabXaccess.tool.model.IdleState
import cloud.fabX.fabXaccess.tool.model.ToolDetailsChanged
import cloud.fabX.fabXaccess.tool.model.ToolFixture
import cloud.fabX.fabXaccess.tool.model.ToolIdFixture
import cloud.fabX.fabXaccess.tool.model.ToolRepository
import cloud.fabX.fabXaccess.tool.model.ToolType
import cloud.fabX.fabXaccess.user.model.AdminFixture
import isNone
import isSome
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
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

    private val fixedInstant = Clock.System.now()
    private val fixedClock = FixedClock(fixedInstant)

    private lateinit var logger: Logger
    private lateinit var toolRepository: ToolRepository
    private lateinit var gettingQualificationById: GettingQualificationById

    private lateinit var testee: ChangingTool

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock toolRepository: ToolRepository,
        @Mock gettingQualificationById: GettingQualificationById
    ) {
        this.logger = logger
        this.toolRepository = toolRepository
        this.gettingQualificationById = gettingQualificationById

        testee = ChangingTool({ logger }, toolRepository, gettingQualificationById, fixedClock)
    }

    @Test
    fun `given tool can be found when changing tool details then sourcing event is created and stored`() = runTest {
        // given
        val tool = ToolFixture.arbitrary(toolId, aggregateVersion = 1)

        val newName = ChangeableValue.ChangeToValueString("newName")
        val newType = ChangeableValue.ChangeToValueToolType(ToolType.UNLOCK)
        val newRequires2FA = ChangeableValue.ChangeToValueBoolean(true)
        val newTime = ChangeableValue.ChangeToValueInt(42)
        val newIdleState = ChangeableValue.ChangeToValueIdleState(IdleState.IDLE_LOW)
        val newEnabled = ChangeableValue.ChangeToValueBoolean(false)
        val newWikiLink = ChangeableValue.LeaveAsIs
        val newNotes = ChangeableValue.ChangeToValueString("hello world")
        val newRequiredQualifications = ChangeableValue.LeaveAsIs

        val expectedSourcingEvent = ToolDetailsChanged(
            toolId,
            2,
            adminActor.id,
            fixedInstant,
            correlationId,
            newName,
            newType,
            newRequires2FA,
            newTime,
            newIdleState,
            newEnabled,
            newNotes,
            newWikiLink,
            newRequiredQualifications
        )

        whenever(toolRepository.getById(toolId))
            .thenReturn(tool.right())

        whenever(toolRepository.store(expectedSourcingEvent))
            .thenReturn(None)

        // when
        val result = testee.changeToolDetails(
            adminActor,
            correlationId,
            toolId,
            newName,
            newType,
            newRequires2FA,
            newTime,
            newIdleState,
            newEnabled,
            newNotes,
            newWikiLink,
            newRequiredQualifications
        )

        // then
        assertThat(result).isNone()

        val inOrder = inOrder(toolRepository)
        inOrder.verify(toolRepository).getById(toolId)
        inOrder.verify(toolRepository).store(expectedSourcingEvent)
    }

    @Test
    fun `given tool cannot be found when changing tool details then returns error`() = runTest {
        // given
        val error = ErrorFixture.arbitrary()

        whenever(toolRepository.getById(toolId))
            .thenReturn(error.left())

        // when
        val result = testee.changeToolDetails(
            adminActor,
            correlationId,
            toolId,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs,
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
    fun `given sourcing event cannot be stored when changing tool details then returns error`() = runTest {
        // given
        val tool = ToolFixture.arbitrary(toolId, aggregateVersion = 1)

        val event = ToolDetailsChanged(
            toolId,
            2,
            adminActor.id,
            fixedInstant,
            correlationId,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs
        )

        val error = ErrorFixture.arbitrary()

        whenever(toolRepository.getById(toolId))
            .thenReturn(tool.right())

        whenever(toolRepository.store(event))
            .thenReturn(error.some())

        // when
        val result = testee.changeToolDetails(
            adminActor,
            correlationId,
            toolId,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs,
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