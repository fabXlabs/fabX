package cloud.fabX.fabXaccess.tool.application

import arrow.core.None
import arrow.core.left
import arrow.core.right
import arrow.core.some
import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.model.ErrorFixture
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.tool.model.ToolDeleted
import cloud.fabX.fabXaccess.tool.model.ToolFixture
import cloud.fabX.fabXaccess.tool.model.ToolIdFixture
import cloud.fabX.fabXaccess.tool.model.ToolRepository
import cloud.fabX.fabXaccess.user.model.AdminFixture
import isNone
import isSome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.whenever

@MockitoSettings
internal class DeletingToolTest {

    private val adminActor = AdminFixture.arbitrary()

    private val toolId = ToolIdFixture.arbitrary()

    private var logger: Logger? = null
    private var toolRepository: ToolRepository? = null

    private var testee: DeletingTool? = null

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock toolRepository: ToolRepository
    ) {
        this.logger = logger
        this.toolRepository = toolRepository
        DomainModule.configureLoggerFactory { logger }
        DomainModule.configureToolRepository(toolRepository)

        testee = DeletingTool()
    }

    @Test
    fun `given tool can be found when deleting tool then sourcing event is created and stored`() {
        // given
        val tool = ToolFixture.arbitrary(toolId, aggregateVersion = 234)

        val expectedSourcingEvent = ToolDeleted(
            toolId,
            235,
            adminActor.id
        )

        whenever(toolRepository!!.getById(toolId))
            .thenReturn(tool.right())

        whenever(toolRepository!!.store(expectedSourcingEvent))
            .thenReturn(None)

        // when
        val result = testee!!.deleteTool(
            adminActor,
            toolId
        )

        // then
        assertThat(result).isNone()
    }

    @Test
    fun `given tool cannot be found when deleting tool then returns error`() {
        // given
        val error = ErrorFixture.arbitrary()

        whenever(toolRepository!!.getById(toolId))
            .thenReturn(error.left())

        // when
        val result = testee!!.deleteTool(
            adminActor,
            toolId
        )

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(error)
    }

    @Test
    fun `given sourcing event cannot be stored when deleting tool then returns error`() {
        // given
        val tool = ToolFixture.arbitrary(toolId, aggregateVersion = 567)

        val event = ToolDeleted(
            toolId,
            568,
            adminActor.id
        )

        val error = ErrorFixture.arbitrary()


        whenever(toolRepository!!.getById(toolId))
            .thenReturn(tool.right())

        whenever(toolRepository!!.store(event))
            .thenReturn(error.some())

        // when
        val result = testee!!.deleteTool(
            adminActor,
            toolId
        )

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(error)
    }
}