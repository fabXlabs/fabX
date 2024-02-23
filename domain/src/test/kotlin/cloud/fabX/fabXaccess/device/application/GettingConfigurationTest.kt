package cloud.fabX.fabXaccess.device.application

import FixedClock
import arrow.core.None
import arrow.core.left
import arrow.core.right
import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.DeviceId
import cloud.fabX.fabXaccess.common.model.ErrorFixture
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.model.TaggedCounter
import cloud.fabX.fabXaccess.common.model.ToolId
import cloud.fabX.fabXaccess.device.model.ActualFirmwareVersionChanged
import cloud.fabX.fabXaccess.device.model.DeviceActor
import cloud.fabX.fabXaccess.device.model.DeviceFixture
import cloud.fabX.fabXaccess.device.model.DeviceIdFixture
import cloud.fabX.fabXaccess.device.model.DeviceRepository
import cloud.fabX.fabXaccess.tool.model.GettingToolById
import cloud.fabX.fabXaccess.tool.model.ToolFixture
import cloud.fabX.fabXaccess.tool.model.ToolIdFixture
import isLeft
import isRight
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.whenever

@MockitoSettings
internal class GettingConfigurationTest {

    private val correlationId = CorrelationIdFixture.arbitrary()

    private lateinit var logger: Logger
    private lateinit var deviceRepository: DeviceRepository
    private lateinit var toolRepository: GettingToolById
    private lateinit var gettingConfigurationCounter: TaggedCounter<DeviceId>

    private val fixedInstant = Clock.System.now()
    private val fixedClock = FixedClock(fixedInstant)

    private lateinit var testee: GettingConfiguration

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock deviceRepository: DeviceRepository,
        @Mock gettingToolById: GettingToolById,
        @Mock gettingConfigurationCounter: TaggedCounter<DeviceId>
    ) {
        this.logger = logger
        this.deviceRepository = deviceRepository
        this.toolRepository = gettingToolById

        testee = GettingConfiguration({ logger }, deviceRepository, gettingToolById, gettingConfigurationCounter, fixedClock)
    }

    @Nested
    internal inner class GivenDeviceAndTools {

        private val deviceId = DeviceIdFixture.arbitrary()
        private val pin1 = 0
        private val pin2 = 1
        private val toolId1 = ToolIdFixture.arbitrary()
        private val toolId2 = ToolIdFixture.arbitrary()

        @BeforeEach
        fun `mock device repository`() {
            val device = DeviceFixture.arbitrary(
                deviceId,
                name = "device42",
                background = "https://example.com/bg.bmp",
                backupBackendUrl = "https://backup.example.com",
                attachedTools = mapOf(pin1 to toolId1, pin2 to toolId2),
                actualFirmwareVersion = "0.0.0"
            )

            runBlocking {
                whenever(deviceRepository.getById(deviceId))
                    .thenReturn(device.right())
            }
        }

        @BeforeEach
        fun `mock tool repository`() {
            runTest {
                val tool1 = ToolFixture.arbitrary(
                    toolId1,
                    name = "toolname1",
                    time = 1
                )
                whenever(toolRepository.getToolById(toolId1))
                    .thenReturn(tool1.right())


                val tool2 = ToolFixture.arbitrary(
                    toolId2,
                    name = "toolname2",
                    time = 2
                )

                whenever(toolRepository.getToolById(toolId2))
                    .thenReturn(tool2.right())
            }
        }

        @Test
        fun `when getting configuration then returns configuration`() = runTest {
            // given
            val deviceActor = DeviceActor(deviceId, "aabbccddee42")

            // when
            val result = testee.getConfiguration(
                deviceActor,
                correlationId,
                "0.0.0"
            )

            // then
            assertThat(result)
                .isRight()
                .all {
                    transform { it.device.id }.isEqualTo(deviceId)
                    transform { it.device.name }.isEqualTo("device42")
                    transform { it.device.background }.isEqualTo("https://example.com/bg.bmp")

                    transform { it.attachedTools.keys }.containsExactlyInAnyOrder(pin1, pin2)

                    transform { it.attachedTools[pin1]!!.id }.isEqualTo(toolId1)
                    transform { it.attachedTools[pin1]!!.name }.isEqualTo("toolname1")
                    transform { it.attachedTools[pin1]!!.time }.isEqualTo(1)

                    transform { it.attachedTools[pin2]!!.id }.isEqualTo(toolId2)
                    transform { it.attachedTools[pin2]!!.name }.isEqualTo("toolname2")
                    transform { it.attachedTools[pin2]!!.time }.isEqualTo(2)
                }
        }

        @Test
        fun `when getting configuration then actual firmware version is updated`() = runTest {
            // given
            val deviceActor = DeviceActor(deviceId, "aabbccddee42")

            val expectedEvent = ActualFirmwareVersionChanged(
                deviceId,
                2,
                deviceId,
                fixedInstant,
                correlationId,
                "1.42.0"
            )

            whenever(deviceRepository.store(expectedEvent))
                .thenReturn(None)

            // when
            val result = testee.getConfiguration(
                deviceActor,
                correlationId,
                "1.42.0"
            )

            // then
            assertThat(result)
                .isRight()
        }
    }

    @Test
    fun `given device cannot be found when getting configuration then returns error`() = runTest {
        // given
        val unknownDeviceId = DeviceIdFixture.arbitrary()
        val unknownDeviceActor = DeviceActor(unknownDeviceId, "aabbccddeeff")

        val expectedError = ErrorFixture.arbitrary()

        whenever(deviceRepository.getById(unknownDeviceId))
            .thenReturn(expectedError.left())

        // when
        val result = testee.getConfiguration(
            unknownDeviceActor,
            correlationId,
            "42.0.0"
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(expectedError)
    }

    @Test
    fun `given tool cannot be found when getting configuration then returns error`() = runTest {
        // given
        val deviceId = DeviceIdFixture.arbitrary()
        val toolId = ToolIdFixture.arbitrary()

        val device = DeviceFixture.arbitrary(
            deviceId,
            name = "device42",
            background = "https://example.com/bg.bmp",
            backupBackendUrl = "https://backup.example.com",
            attachedTools = mapOf(0 to toolId),
            actualFirmwareVersion = "0.0.0"
        )

        val error = ErrorFixture.arbitrary()

        val deviceActor = DeviceActor(deviceId, "aabbccddee42")

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(device.right())

        whenever(toolRepository.getToolById(toolId))
            .thenReturn(error.left())

        // when
        val result = testee.getConfiguration(
            deviceActor,
            correlationId,
            "0.0.0"
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }
}