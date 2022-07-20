package cloud.fabX.fabXaccess.tool.application

import arrow.core.None
import arrow.core.some
import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.ErrorFixture
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFixture
import cloud.fabX.fabXaccess.tool.model.IdleState
import cloud.fabX.fabXaccess.tool.model.ToolCreated
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
import org.mockito.kotlin.whenever

@MockitoSettings
internal class AddingToolTest {

    private val adminActor = AdminFixture.arbitrary()
    private val correlationId = CorrelationIdFixture.arbitrary()

    private val toolId = ToolIdFixture.arbitrary()

    private var logger: Logger? = null
    private var toolRepository: ToolRepository? = null

    private var testee: AddingTool? = null

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock toolRepository: ToolRepository
    ) {
        this.logger = logger
        this.toolRepository = toolRepository

        DomainModule.configureLoggerFactory { logger }
        DomainModule.configureToolIdFactory { toolId }
        DomainModule.configureToolRepository(toolRepository)

        testee = AddingTool()
    }

    @Test
    fun `given valid values when adding tool then sourcing event is created and stored`() {
        // given
        val name = "Door Shop"
        val toolType = ToolType.UNLOCK
        val time = 200
        val idleState = IdleState.IDLE_HIGH
        val wikiUrl = "https://example.com/shopdoor"
        val requiredQualifications = setOf(QualificationIdFixture.arbitrary())

        val expectedSourcingEvent = ToolCreated(
            toolId,
            adminActor.id,
            correlationId,
            name,
            toolType,
            time,
            idleState,
            wikiUrl,
            requiredQualifications,
        )

        whenever(toolRepository!!.store(expectedSourcingEvent))
            .thenReturn(None)

        // when
        val result = testee!!.addTool(
            adminActor,
            correlationId,
            name,
            toolType,
            time,
            idleState,
            wikiUrl,
            requiredQualifications,
        )

        // then
        assertThat(result).isNone()
    }

    @Test
    fun `given sourcing event cannot be stored when adding tool then returns error`() {
        // given
        val name = "Door Shop"
        val toolType = ToolType.UNLOCK
        val time = 200
        val idleState = IdleState.IDLE_HIGH
        val wikiUrl = "https://example.com/shopdoor"
        val requiredQualifications = setOf(QualificationIdFixture.arbitrary())

        val event = ToolCreated(
            toolId,
            adminActor.id,
            correlationId,
            name,
            toolType,
            time,
            idleState,
            wikiUrl,
            requiredQualifications,
        )

        val error = ErrorFixture.arbitrary()

        whenever(toolRepository!!.store(event))
            .thenReturn(error.some())

        // when
        val result = testee!!.addTool(
            adminActor,
            correlationId,
            name,
            toolType,
            time,
            idleState,
            wikiUrl,
            requiredQualifications,
        )

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(error)
    }
}