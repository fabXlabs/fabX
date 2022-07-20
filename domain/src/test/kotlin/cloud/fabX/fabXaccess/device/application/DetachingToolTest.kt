package cloud.fabX.fabXaccess.device.application

import arrow.core.None
import arrow.core.left
import arrow.core.right
import arrow.core.some
import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.ErrorFixture
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.device.model.DeviceFixture
import cloud.fabX.fabXaccess.device.model.DeviceIdFixture
import cloud.fabX.fabXaccess.device.model.DeviceRepository
import cloud.fabX.fabXaccess.device.model.ToolDetached
import cloud.fabX.fabXaccess.tool.model.ToolIdFixture
import cloud.fabX.fabXaccess.user.model.AdminFixture
import isNone
import isSome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.whenever

@MockitoSettings
internal class DetachingToolTest {

    private val adminActor = AdminFixture.arbitrary()

    private val deviceId = DeviceIdFixture.arbitrary()

    private var logger: Logger? = null
    private var deviceRepository: DeviceRepository? = null

    private var testee: DetachingTool? = null

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock deviceRepository: DeviceRepository
    ) {
        this.logger = logger
        this.deviceRepository = deviceRepository
        DomainModule.configureLoggerFactory { logger }
        DomainModule.configureDeviceRepository(deviceRepository)

        testee = DetachingTool()
    }

    @Test
    fun `given device can be found when detaching tool then sourcing event is created and stored`() {
        // given
        val toolId = ToolIdFixture.arbitrary()

        val pin = 3
        val device = DeviceFixture.arbitrary(
            deviceId,
            aggregateVersion = 1,
            attachedTools = mapOf(pin to toolId)
        )

        val expectedSourcingEvent = ToolDetached(
            deviceId,
            2,
            adminActor.id,
            pin
        )

        whenever(deviceRepository!!.getById(deviceId))
            .thenReturn(device.right())

        whenever(deviceRepository!!.store(expectedSourcingEvent))
            .thenReturn(None)

        // when
        val result = testee!!.detachTool(
            adminActor,
            deviceId,
            pin
        )

        // then
        assertThat(result).isNone()
    }

    @Test
    fun `given domain error when detaching tool then returns domain error`() {
        // given
        val device = DeviceFixture.arbitrary(
            deviceId,
            aggregateVersion = 1,
            attachedTools = mapOf()
        )

        whenever(deviceRepository!!.getById(deviceId))
            .thenReturn(device.right())

        val pin = 42
        val expectedDomainError = Error.PinNotInUse("No tool attached at pin $pin.", pin)

        // when
        val result = testee!!.detachTool(
            adminActor,
            deviceId,
            pin
        )

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(expectedDomainError)
    }

    @Test
    fun `given tool cannot be found when detaching tool then returns error`() {
        // given
        val error = Error.DeviceNotFound("", deviceId)

        whenever(deviceRepository!!.getById(deviceId))
            .thenReturn(error.left())

        // when
        val result = testee!!.detachTool(
            adminActor,
            deviceId,
            42
        )

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(error)
    }

    @Test
    fun `given sourcing event cannot be stored when attaching tool then returns error`() {
        // given
        val toolId = ToolIdFixture.arbitrary()

        val pin = 3
        val device = DeviceFixture.arbitrary(
            deviceId,
            aggregateVersion = 1,
            attachedTools = mapOf(pin to toolId)
        )

        val expectedSourcingEvent = ToolDetached(
            deviceId,
            2,
            adminActor.id,
            pin
        )

        val error = ErrorFixture.arbitrary()

        whenever(deviceRepository!!.getById(deviceId))
            .thenReturn(device.right())

        whenever(deviceRepository!!.store(expectedSourcingEvent))
            .thenReturn(error.some())

        // when
        val result = testee!!.detachTool(
            adminActor,
            deviceId,
            pin
        )

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(error)
    }
}