package cloud.fabX.fabXaccess.tool.application

import FixedClock
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.DomainEventPublisher
import cloud.fabX.fabXaccess.common.model.ErrorFixture
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.tool.model.ToolDeleted
import cloud.fabX.fabXaccess.tool.model.ToolFixture
import cloud.fabX.fabXaccess.tool.model.ToolIdFixture
import cloud.fabX.fabXaccess.tool.model.ToolRepository
import cloud.fabX.fabXaccess.user.model.AdminFixture
import isLeft
import isRight
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@MockitoSettings
internal class DeletingToolTest {

    private val adminActor = AdminFixture.arbitrary()
    private val correlationId = CorrelationIdFixture.arbitrary()

    private val toolId = ToolIdFixture.arbitrary()
    private val fixedInstant = Clock.System.now()
    private val fixedClock = FixedClock(fixedInstant)

    private lateinit var logger: Logger
    private lateinit var domainEventPublisher: DomainEventPublisher
    private lateinit var toolRepository: ToolRepository

    private lateinit var testee: DeletingTool

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock domainEventPublisher: DomainEventPublisher,
        @Mock toolRepository: ToolRepository
    ) {
        this.logger = logger
        this.domainEventPublisher = domainEventPublisher
        this.toolRepository = toolRepository

        testee = DeletingTool({ logger }, fixedClock, domainEventPublisher, toolRepository)
    }

    @Test
    fun `given tool can be found when deleting tool then sourcing event is created and stored and domain event is published`() =
        runTest {
            // given
            val tool = ToolFixture.arbitrary(toolId, aggregateVersion = 234)

            val expectedSourcingEvent = ToolDeleted(
                toolId,
                235,
                adminActor.id,
                fixedInstant,
                correlationId
            )

            whenever(toolRepository.getById(toolId))
                .thenReturn(tool.right())

            whenever(toolRepository.store(expectedSourcingEvent))
                .thenReturn(Unit.right())

            // when
            val result = testee.deleteTool(
                adminActor,
                correlationId,
                toolId
            )

            // then
            assertThat(result)
                .isRight()
                .isEqualTo(Unit)
            verify(domainEventPublisher).publish(
                cloud.fabX.fabXaccess.common.model.ToolDeleted(
                    adminActor.id,
                    fixedInstant,
                    correlationId,
                    toolId
                )
            )
        }

    @Test
    fun `given tool cannot be found when deleting tool then returns error`() = runTest {
        // given
        val error = ErrorFixture.arbitrary()

        whenever(toolRepository.getById(toolId))
            .thenReturn(error.left())

        // when
        val result = testee.deleteTool(
            adminActor,
            correlationId,
            toolId
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }

    @Test
    fun `given sourcing event cannot be stored when deleting tool then returns error`() = runTest {
        // given
        val tool = ToolFixture.arbitrary(toolId, aggregateVersion = 567)

        val event = ToolDeleted(
            toolId,
            568,
            adminActor.id,
            fixedInstant,
            correlationId
        )

        val error = ErrorFixture.arbitrary()


        whenever(toolRepository.getById(toolId))
            .thenReturn(tool.right())

        whenever(toolRepository.store(event))
            .thenReturn(error.left())

        // when
        val result = testee.deleteTool(
            adminActor,
            correlationId,
            toolId
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }
}