package cloud.fabX.fabXaccess.user.application

import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.model.QualificationId
import cloud.fabX.fabXaccess.device.model.DeviceActor
import cloud.fabX.fabXaccess.device.model.DeviceFixture
import cloud.fabX.fabXaccess.device.model.DeviceIdFixture
import cloud.fabX.fabXaccess.device.model.DeviceRepository
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFixture
import cloud.fabX.fabXaccess.tool.model.Tool
import cloud.fabX.fabXaccess.tool.model.ToolFixture
import cloud.fabX.fabXaccess.tool.model.ToolIdFixture
import cloud.fabX.fabXaccess.tool.model.ToolRepository
import cloud.fabX.fabXaccess.user.model.Member
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import isLeft
import isRight
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mock
import org.mockito.Mockito.lenient
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@MockitoSettings
internal class GettingAuthorizedToolsTest {
    private val correlationId = CorrelationIdFixture.arbitrary()

    private lateinit var logger: Logger
    private lateinit var deviceRepository: DeviceRepository
    private lateinit var toolRepository: ToolRepository

    private lateinit var testee: GettingAuthorizedTools

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock deviceRepository: DeviceRepository,
        @Mock toolRepository: ToolRepository
    ) {
        this.logger = logger
        this.deviceRepository = deviceRepository
        this.toolRepository = toolRepository

        testee = GettingAuthorizedTools({ logger }, deviceRepository, toolRepository)
    }

    companion object {
        private val qualification1Id = QualificationIdFixture.arbitrary()
        private val qualification2Id = QualificationIdFixture.arbitrary()

        private val tool1Id = ToolIdFixture.arbitrary()
        private val tool1 = ToolFixture.arbitrary(tool1Id, requiredQualifications = setOf(qualification1Id))
        private val tool2Id = ToolIdFixture.arbitrary()
        private val tool2 = ToolFixture.arbitrary(tool2Id, requiredQualifications = setOf(qualification2Id))
        private val tool3Id = ToolIdFixture.arbitrary()
        private val tool3 =
            ToolFixture.arbitrary(tool3Id, requiredQualifications = setOf(qualification1Id, qualification2Id))
        private val tool4Id = ToolIdFixture.arbitrary()
        private val tool4 = ToolFixture.arbitrary(tool4Id, requiredQualifications = setOf())

        @JvmStatic
        private fun bla(): Iterable<Arguments> {
            return listOf(
                Arguments.of(
                    setOf(qualification1Id, qualification2Id),
                    setOf(tool1, tool2, tool3, tool4)
                ),
                Arguments.of(
                    setOf(qualification1Id),
                    setOf(tool1, tool4)
                ),
                Arguments.of(
                    setOf(qualification2Id),
                    setOf(tool2, tool4)
                ),
                Arguments.of(
                    setOf<QualificationId>(),
                    setOf(tool4)
                )
            )
        }
    }

    @ParameterizedTest
    @MethodSource("bla")
    fun `when getting authorized tools then returns authorized tools`(
        memberQualifications: Set<QualificationId>,
        expectedAuthorizedTools: Set<Tool>
    ) =
        runTest {
            // given
            val deviceId = DeviceIdFixture.arbitrary()
            val userId = UserIdFixture.arbitrary()
            val member = Member(userId, "some member", memberQualifications)
            val deviceActorOnBehalfOfMember = DeviceActor(deviceId, "aabbcc112233", onBehalfOf = member)

            val device = DeviceFixture.arbitrary(
                attachedTools = mapOf(1 to tool1Id, 2 to tool2Id, 3 to tool3Id, 4 to tool4Id)
            )

            whenever(deviceRepository.getById(deviceId))
                .thenReturn(device.right())

            lenient().`when`(toolRepository.getById(tool1Id))
                .thenReturn(tool1.right())

            lenient().`when`(toolRepository.getById(tool2Id))
                .thenReturn(tool2.right())

            lenient().`when`(toolRepository.getById(tool3Id))
                .thenReturn(tool3.right())

            lenient().`when`(toolRepository.getById(tool4Id))
                .thenReturn(tool4.right())

            // when
            val result = testee.getAuthorizedTools(deviceActorOnBehalfOfMember, correlationId)

            // then
            assertThat(result)
                .isRight()
                .isEqualTo(expectedAuthorizedTools)
        }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `given tool is disabled when getting authorized tools then does not return tool`(
        tool2Enabled: Boolean
    ) = runTest {
        // given
        val deviceId = DeviceIdFixture.arbitrary()
        val userId = UserIdFixture.arbitrary()
        val member = Member(userId, "some member", setOf())
        val deviceActorOnBehalfOfMember = DeviceActor(deviceId, "aabbcc112233", onBehalfOf = member)

        val tool1 = ToolFixture.arbitrary(tool1Id, enabled = true, requiredQualifications = setOf())
        val tool2 = ToolFixture.arbitrary(tool2Id, enabled = tool2Enabled, requiredQualifications = setOf())

        val device = DeviceFixture.arbitrary(
            attachedTools = mapOf(1 to tool1Id, 2 to tool2Id)
        )

        whenever(deviceRepository.getById(deviceId))
            .thenReturn(device.right())

        whenever(toolRepository.getById(tool1Id))
            .thenReturn(tool1.right())

        whenever(toolRepository.getById(tool2Id))
            .thenReturn(tool2.right())

        // when
        val result = testee.getAuthorizedTools(deviceActorOnBehalfOfMember, correlationId)

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(
                if (tool2Enabled) {
                    setOf(tool1, tool2)
                } else {
                    setOf(tool1)
                }
            )
    }

    @Test
    fun `given not acting on behalf of member when getting authorized tools then returns error`() = runTest {
        // given
        val deviceActor = DeviceActor(DeviceIdFixture.arbitrary(), "aabbcc112233", onBehalfOf = null)

        // when
        val result = testee.getAuthorizedTools(deviceActor, correlationId)

        // then
        assertThat(result)
            .isLeft()
            .isInstanceOf(Error.NotAuthenticated::class)
            .transform { it.message }
            .isEqualTo("Required authentication not found.")
    }
}