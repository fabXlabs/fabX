package cloud.fabX.fabXaccess.user.application

import FixedClock
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.application.withSecondPrecision
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.model.TaggedCounter
import cloud.fabX.fabXaccess.common.model.ToolId
import cloud.fabX.fabXaccess.device.model.DeviceFixture
import cloud.fabX.fabXaccess.tool.model.ToolIdFixture
import cloud.fabX.fabXaccess.tool.model.ToolRepository
import cloud.fabX.fabXaccess.user.model.ToolUsageLogEntry
import cloud.fabX.fabXaccess.user.model.ToolUsageLogRepository
import cloud.fabX.fabXaccess.user.model.UserFixture
import isLeft
import isRight
import kotlinx.coroutines.test.runTest
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.whenever

@OptIn(ExperimentalTime::class)
@MockitoSettings
internal class LoggingUnlockedToolTest {
    private val user = UserFixture.arbitrary()

    private val device = DeviceFixture.arbitrary()
    private val deviceActor = device.asActor().copy(onBehalfOf = user.asMember())

    private val toolId = ToolIdFixture.arbitrary()
    private val correlationId = CorrelationIdFixture.arbitrary()

    private lateinit var logger: Logger
    private lateinit var toolUsageCounter: TaggedCounter<ToolId>
    private lateinit var toolRepository: ToolRepository
    private lateinit var toolUsageLogRepository: ToolUsageLogRepository

    private val fixedInstant = Clock.System.now().withSecondPrecision()
    private val fixedClock = FixedClock(fixedInstant)

    private lateinit var testee: LoggingUnlockedTool

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock toolUsageCounter: TaggedCounter<ToolId>,
        @Mock toolRepository: ToolRepository,
        @Mock toolUsageLogRepository: ToolUsageLogRepository,
    ) {
        this.logger = logger
        this.toolUsageCounter = toolUsageCounter
        this.toolRepository = toolRepository
        this.toolUsageLogRepository = toolUsageLogRepository

        testee = LoggingUnlockedTool(toolUsageCounter, toolRepository, toolUsageLogRepository, fixedClock, { logger })
    }

    @Test
    fun `when logging unlocked tool then usage is logged and stored`() = runTest {
        // given
        val expectedToolUsageLogEntry = ToolUsageLogEntry(fixedInstant, user.id, toolId)

        whenever(toolUsageLogRepository.store(expectedToolUsageLogEntry))
            .thenReturn(Unit.right())

        // when
        val result = testee.logUnlockedTool(
            actor = deviceActor,
            toolId = toolId,
            correlationId = correlationId
        )

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(Unit)
    }

    @Test
    fun `given missing user when logging unlocked tool then returns error`() = runTest {
        // given
        val device = DeviceFixture.arbitrary()

        val expectedError = Error.NotAuthenticated("Required authentication not found.", correlationId)

        // when
        val result = testee.logUnlockedTool(
            actor = device.asActor(),
            toolId = toolId,
            correlationId = correlationId
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(expectedError)
    }
}
