package cloud.fabX.fabXaccess.tool.application

import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isSameAs
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.ErrorFixture
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.tool.model.ToolFixture
import cloud.fabX.fabXaccess.tool.model.ToolIdFixture
import cloud.fabX.fabXaccess.tool.model.ToolRepository
import cloud.fabX.fabXaccess.user.model.InstructorFixture
import isLeft
import isRight
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.whenever

@MockitoSettings
internal class GettingToolTest {

    private val actor = InstructorFixture.arbitrary()
    private val toolId = ToolIdFixture.arbitrary()
    private val correlationId = CorrelationIdFixture.arbitrary()

    private lateinit var logger: Logger
    private lateinit var toolRepository: ToolRepository

    private lateinit var testee: GettingTool

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock toolRepository: ToolRepository
    ) {
        this.logger = logger
        this.toolRepository = toolRepository

        testee = GettingTool({ logger }, toolRepository)
    }

    @Test
    fun `when getting all then returns all from repository`() {
        // given
        val tool1 = ToolFixture.arbitrary()
        val tool2 = ToolFixture.arbitrary()

        val tools = setOf(tool1, tool2)

        whenever(toolRepository.getAll())
            .thenReturn(tools)

        // when
        val result = testee.getAll(actor, correlationId)

        // then
        assertThat(result)
            .isSameAs(tools)
    }

    @Test
    fun `given tool exists when getting by id then returns from repository`() {
        // given
        val tool = ToolFixture.arbitrary()

        whenever(toolRepository.getById(toolId))
            .thenReturn(tool.right())

        // when
        val result = testee.getById(actor, correlationId, toolId)

        // then
        assertThat(result)
            .isRight()
            .isSameAs(tool)
    }

    @Test
    fun `given repository error when getting by id then returns error`() {
        // given
        val expectedError = ErrorFixture.arbitrary()

        whenever(toolRepository.getById(toolId))
            .thenReturn(expectedError.left())

        // when
        val result = testee.getById(actor, correlationId, toolId)

        // then
        assertThat(result)
            .isLeft()
            .isSameAs(expectedError)
    }
}