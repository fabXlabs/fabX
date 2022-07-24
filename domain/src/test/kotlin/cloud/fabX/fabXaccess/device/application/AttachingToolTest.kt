package cloud.fabX.fabXaccess.device.application

import arrow.core.None
import arrow.core.left
import arrow.core.right
import arrow.core.some
import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.ErrorFixture
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.device.model.DeviceFixture
import cloud.fabX.fabXaccess.device.model.DeviceIdFixture
import cloud.fabX.fabXaccess.device.model.DeviceRepository
import cloud.fabX.fabXaccess.device.model.ToolAttached
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
internal class AttachingToolTest {

    private val adminActor = AdminFixture.arbitrary()
    private val correlationId = CorrelationIdFixture.arbitrary()

    private val deviceId = DeviceIdFixture.arbitrary()

    private var logger: Logger? = null
    private var deviceRepository: DeviceRepository? = null
    private var toolRepository: ToolRepository? = null

    private var testee: AttachingTool? = null

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock deviceRepository: DeviceRepository,
        @Mock toolRepository: ToolRepository
    ) {
        this.logger = logger
        this.deviceRepository = deviceRepository
        this.toolRepository = toolRepository
        DomainModule.configureLoggerFactory { logger }
        DomainModule.configureDeviceRepository(deviceRepository)
        DomainModule.configureToolRepository(toolRepository)

        testee = AttachingTool()
    }

    @Test
    fun `given device can be found when attaching tool then sourcing event is created and stored`() {
        // given
        val device = DeviceFixture.arbitrary(deviceId, aggregateVersion = 1)

        val toolId = ToolIdFixture.arbitrary()
        val tool = ToolFixture.arbitrary(toolId)

        val pin = 42

        val expectedSourcingEvent = ToolAttached(
            deviceId,
            2,
            adminActor.id,
            correlationId,
            pin,
            toolId
        )

        whenever(deviceRepository!!.getById(deviceId))
            .thenReturn(device.right())

        whenever(toolRepository!!.getToolById(toolId))
            .thenReturn(tool.right())

        whenever(deviceRepository!!.store(expectedSourcingEvent))
            .thenReturn(None)

        // when
        val result = testee!!.attachTool(
            adminActor,
            correlationId,
            deviceId,
            pin,
            toolId
        )

        // then
        assertThat(result).isNone()
    }

    @Test
    fun `given domain error when attaching tool then returns domain error`() {
        // given
        val device = DeviceFixture.arbitrary(deviceId, aggregateVersion = 1)

        val toolId = ToolIdFixture.arbitrary()

        val expectedDomainError = ErrorFixture.arbitrary()

        whenever(deviceRepository!!.getById(deviceId))
            .thenReturn(device.right())

        whenever(toolRepository!!.getToolById(toolId))
            .thenReturn(expectedDomainError.left())

        // when
        val result = testee!!.attachTool(
            adminActor,
            correlationId,
            deviceId,
            1,
            toolId
        )

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(expectedDomainError)
    }

    @Test
    fun `given device cannot be found when attaching tool then returns error`() {
        // given
        val error = Error.DeviceNotFound("", deviceId)

        whenever(deviceRepository!!.getById(deviceId))
            .thenReturn(error.left())

        // when
        val result = testee!!.attachTool(
            adminActor,
            correlationId,
            deviceId,
            1,
            ToolIdFixture.arbitrary()
        )

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(error)
    }

    @Test
    fun `given sourcing event cannot be stored when attaching tool then returns error`() {
        val device = DeviceFixture.arbitrary(deviceId, aggregateVersion = 1)

        val toolId = ToolIdFixture.arbitrary()
        val tool = ToolFixture.arbitrary(toolId)

        val pin = 42

        val expectedSourcingEvent = ToolAttached(
            deviceId,
            2,
            adminActor.id,
            correlationId,
            pin,
            toolId
        )

        val error = ErrorFixture.arbitrary()

        whenever(deviceRepository!!.getById(deviceId))
            .thenReturn(device.right())

        whenever(toolRepository!!.getToolById(toolId))
            .thenReturn(tool.right())

        whenever(deviceRepository!!.store(expectedSourcingEvent))
            .thenReturn(error.some())

        // when
        val result = testee!!.attachTool(
            adminActor,
            correlationId,
            deviceId,
            pin,
            toolId
        )

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(error)
    }
}