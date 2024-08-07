package cloud.fabX.fabXaccess.tool.application

import FixedClock
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.ErrorFixture
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.qualification.model.GettingQualificationById
import cloud.fabX.fabXaccess.qualification.model.QualificationFixture
import cloud.fabX.fabXaccess.tool.model.IdleState
import cloud.fabX.fabXaccess.tool.model.ToolCreated
import cloud.fabX.fabXaccess.tool.model.ToolIdFixture
import cloud.fabX.fabXaccess.tool.model.ToolRepository
import cloud.fabX.fabXaccess.tool.model.ToolType
import cloud.fabX.fabXaccess.user.model.AdminFixture
import isLeft
import isRight
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.whenever

@MockitoSettings
internal class AddingToolTest {

    private val adminActor = AdminFixture.arbitrary()
    private val correlationId = CorrelationIdFixture.arbitrary()

    private val toolId = ToolIdFixture.arbitrary()

    private val fixedInstant = Clock.System.now()
    private val fixedClock = FixedClock(fixedInstant)

    private lateinit var logger: Logger
    private lateinit var toolRepository: ToolRepository
    private lateinit var gettingQualificationById: GettingQualificationById

    private lateinit var testee: AddingTool

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock toolRepository: ToolRepository,
        @Mock gettingQualificationById: GettingQualificationById
    ) {
        this.logger = logger
        this.toolRepository = toolRepository
        this.gettingQualificationById = gettingQualificationById

        testee = AddingTool({ logger }, toolRepository, { toolId }, gettingQualificationById, fixedClock)
    }

    @Test
    fun `given valid values when adding tool then sourcing event is created and stored`() = runTest {
        // given
        val name = "Door Shop"
        val toolType = ToolType.UNLOCK
        val requires2FA = false
        val time = 200
        val idleState = IdleState.IDLE_HIGH
        val wikiUrl = "https://example.com/shopdoor"
        val qualification = QualificationFixture.arbitrary()
        val requiredQualifications = setOf(qualification.id)

        val expectedSourcingEvent = ToolCreated(
            toolId,
            adminActor.id,
            fixedInstant,
            correlationId,
            name,
            toolType,
            requires2FA,
            time,
            idleState,
            wikiUrl,
            requiredQualifications,
        )

        whenever(gettingQualificationById.getQualificationById(qualification.id))
            .thenReturn(qualification.right())

        whenever(toolRepository.store(expectedSourcingEvent))
            .thenReturn(Unit.right())

        // when
        val result = testee.addTool(
            adminActor,
            correlationId,
            name,
            toolType,
            requires2FA,
            time,
            idleState,
            wikiUrl,
            requiredQualifications,
        )

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(toolId)
    }

    @Test
    fun `given sourcing event cannot be stored when adding tool then returns error`() = runTest {
        // given
        val name = "Door Shop"
        val toolType = ToolType.UNLOCK
        val requires2FA = false
        val time = 200
        val idleState = IdleState.IDLE_HIGH
        val wikiUrl = "https://example.com/shopdoor"
        val qualification = QualificationFixture.arbitrary()
        val requiredQualifications = setOf(qualification.id)

        val event = ToolCreated(
            toolId,
            adminActor.id,
            fixedInstant,
            correlationId,
            name,
            toolType,
            requires2FA,
            time,
            idleState,
            wikiUrl,
            requiredQualifications,
        )

        val error = ErrorFixture.arbitrary()

        whenever(gettingQualificationById.getQualificationById(qualification.id))
            .thenReturn(qualification.right())

        whenever(toolRepository.store(event))
            .thenReturn(error.left())

        // when
        val result = testee.addTool(
            adminActor,
            correlationId,
            name,
            toolType,
            requires2FA,
            time,
            idleState,
            wikiUrl,
            requiredQualifications,
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }

    @Test
    fun `given domain error when adding tool then returns domain error`() = runTest {
        // given
        val name = "Door Shop"
        val toolType = ToolType.UNLOCK
        val requires2FA = false
        val time = 200
        val idleState = IdleState.IDLE_HIGH
        val wikiUrl = "https://example.com/shopdoor"
        val qualification = QualificationFixture.arbitrary()
        val requiredQualifications = setOf(qualification.id)

        val error = ErrorFixture.arbitrary()

        whenever(gettingQualificationById.getQualificationById(qualification.id))
            .thenReturn(error.left())

        // when
        val result = testee.addTool(
            adminActor,
            correlationId,
            name,
            toolType,
            requires2FA,
            time,
            idleState,
            wikiUrl,
            requiredQualifications,
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }
}