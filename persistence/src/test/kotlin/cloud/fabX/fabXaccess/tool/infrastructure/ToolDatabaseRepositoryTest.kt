package cloud.fabX.fabXaccess.tool.infrastructure

import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFixture
import cloud.fabX.fabXaccess.tool.model.GettingToolsByQualificationId
import cloud.fabX.fabXaccess.tool.model.IdleState
import cloud.fabX.fabXaccess.tool.model.ToolCreated
import cloud.fabX.fabXaccess.tool.model.ToolDeleted
import cloud.fabX.fabXaccess.tool.model.ToolDetailsChanged
import cloud.fabX.fabXaccess.tool.model.ToolFixture
import cloud.fabX.fabXaccess.tool.model.ToolIdFixture
import cloud.fabX.fabXaccess.tool.model.ToolRepository
import cloud.fabX.fabXaccess.tool.model.ToolType
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import isLeft
import isNone
import isRight
import isSome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource

internal class ToolDatabaseRepositoryTest {

    companion object {
        private val toolId = ToolIdFixture.static(86134)
        private val toolId2 = ToolIdFixture.static(34567)
        private val toolId3 = ToolIdFixture.static(3456789)

        private val qualificationId = QualificationIdFixture.static(7656)
        private val qualificationId2 = QualificationIdFixture.static(345)

        private val actorId = UserIdFixture.static(987)
        private val correlationId = CorrelationIdFixture.arbitrary()

        @JvmStatic
        fun toolAndQualificationIds(): Iterable<Arguments> {
            return listOf(
                Arguments.of(
                    qualificationId,
                    setOf(toolId, toolId2)
                ),
                Arguments.of(
                    qualificationId2,
                    setOf(toolId2, toolId3)
                )
            )
        }
    }

    @Test
    fun `given empty repository when getting tool by id then returns tool not found error`() {
        // given
        val repository = ToolDatabaseRepository()

        // when
        val result = repository.getById(toolId)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.ToolNotFound(
                    "Tool with id ToolId(value=7d039745-0d78-3b2d-86d8-fdeec2b8a872) not found.",
                    toolId
                )
            )
    }

    @Nested
    internal inner class GivenEventsForToolStoredInRepository {

        private lateinit var repository: ToolRepository

        @BeforeEach
        fun setup() {
            repository = ToolDatabaseRepository()

            val event1 = ToolCreated(
                toolId,
                actorId,
                correlationId,
                "name",
                ToolType.UNLOCK,
                42,
                IdleState.IDLE_HIGH,
                "https://wiki.example.com",
                setOf(qualificationId)
            )
            repository.store(event1)

            val event2 = ToolDetailsChanged(
                toolId,
                2,
                actorId,
                correlationId,
                ChangeableValue.ChangeToValue("name2"),
                ChangeableValue.ChangeToValue(ToolType.KEEP),
                ChangeableValue.LeaveAsIs,
                ChangeableValue.LeaveAsIs,
                ChangeableValue.LeaveAsIs,
                ChangeableValue.LeaveAsIs,
                ChangeableValue.LeaveAsIs
            )
            repository.store(event2)
        }

        @Test
        fun `when getting tool by id then returns tool from events`() {
            // given
            val repository = this.repository

            // when
            val result = repository.getById(toolId)

            // then
            assertThat(result)
                .isRight()
                .all {
                    transform { it.id }.isEqualTo(toolId)
                    transform { it.aggregateVersion }.isEqualTo(2)
                    transform { it.name }.isEqualTo("name2")
                    transform { it.type }.isEqualTo(ToolType.KEEP)
                    transform { it.time }.isEqualTo(42)
                    transform { it.idleState }.isEqualTo(IdleState.IDLE_HIGH)
                    transform { it.wikiLink }.isEqualTo("https://wiki.example.com")
                    transform { it.requiredQualifications }.isEqualTo(setOf(qualificationId))
                }
        }

        @Test
        fun `when storing then accepts aggregate version number increased by one`() {
            // given
            val repository = this.repository

            val event = ToolDetailsChanged(
                toolId,
                3,
                actorId,
                correlationId,
                ChangeableValue.ChangeToValue("name3"),
                ChangeableValue.LeaveAsIs,
                ChangeableValue.LeaveAsIs,
                ChangeableValue.LeaveAsIs,
                ChangeableValue.LeaveAsIs,
                ChangeableValue.LeaveAsIs,
                ChangeableValue.LeaveAsIs
            )

            // when
            val result = repository.store(event)

            // then
            assertThat(result).isNone()

            assertThat(repository.getById(toolId))
                .isRight()
                .transform { it.aggregateVersion }
                .isEqualTo(3)
        }

        @ParameterizedTest
        @ValueSource(longs = [-1, 0, 2, 4, 42])
        fun `when storing then not accepts version numbers other than increased by one`(version: Long) {
            // given
            val repository = this.repository

            val event = ToolDetailsChanged(
                toolId,
                version,
                actorId,
                correlationId,
                ChangeableValue.LeaveAsIs,
                ChangeableValue.LeaveAsIs,
                ChangeableValue.LeaveAsIs,
                ChangeableValue.LeaveAsIs,
                ChangeableValue.LeaveAsIs,
                ChangeableValue.LeaveAsIs,
                ChangeableValue.LeaveAsIs
            )

            // when
            val result = repository.store(event)

            // then
            assertThat(result)
                .isSome()
                .isEqualTo(
                    Error.VersionConflict(
                        "Previous version of tool ToolId(value=7d039745-0d78-3b2d-86d8-fdeec2b8a872) is 2, " +
                                "desired new version is $version."
                    )
                )
        }
    }

    @Nested
    internal inner class GivenEventsForToolsStoredInRepository {

        private val toolId2 = ToolIdFixture.static(34567)
        private val toolId3 = ToolIdFixture.static(3456789)

        private val qualificationId2 = QualificationIdFixture.static(345)

        private lateinit var repository: ToolRepository

        @BeforeEach
        fun setup() {
            repository = ToolDatabaseRepository()

            val tool1event1 = ToolCreated(
                toolId,
                actorId,
                correlationId,
                "tool1",
                ToolType.UNLOCK,
                1,
                IdleState.IDLE_HIGH,
                "https://wiki.example.com/tool1",
                setOf(qualificationId)
            )
            repository.store(tool1event1)

            val tool1event2 = ToolDetailsChanged(
                toolId,
                2,
                actorId,
                correlationId,
                ChangeableValue.ChangeToValue("name2"),
                ChangeableValue.ChangeToValue(ToolType.KEEP),
                ChangeableValue.LeaveAsIs,
                ChangeableValue.LeaveAsIs,
                ChangeableValue.LeaveAsIs,
                ChangeableValue.LeaveAsIs,
                ChangeableValue.ChangeToValue(setOf(qualificationId2))
            )
            repository.store(tool1event2)


            val tool2event1 = ToolCreated(
                toolId2,
                actorId,
                correlationId,
                "tool2",
                ToolType.KEEP,
                2,
                IdleState.IDLE_HIGH,
                "https://wiki.example.com/tool2",
                setOf(qualificationId, qualificationId2)
            )
            repository.store(tool2event1)

            val tool3event1 = ToolCreated(
                toolId3,
                actorId,
                correlationId,
                "tool3",
                ToolType.KEEP,
                3,
                IdleState.IDLE_HIGH,
                "https://wiki.example.com/tool3",
                setOf(qualificationId2)
            )
            repository.store(tool3event1)

            val tool2event2 = ToolDeleted(
                toolId2,
                2,
                actorId,
                correlationId
            )
            repository.store(tool2event2)

            val tool3event2 = ToolDetailsChanged(
                toolId3,
                2,
                actorId,
                correlationId,
                ChangeableValue.ChangeToValue("newName3"),
                ChangeableValue.LeaveAsIs,
                ChangeableValue.LeaveAsIs,
                ChangeableValue.LeaveAsIs,
                ChangeableValue.ChangeToValue(false),
                ChangeableValue.ChangeToValue("https://wiki.example.com/newtool3"),
                ChangeableValue.LeaveAsIs
            )
            repository.store(tool3event2)
        }

        @Test
        fun `when getting all tools then returns all tools from events`() {
            // given
            val repository = this.repository

            // when
            val result = repository.getAll()

            // then
            assertThat(result).containsExactlyInAnyOrder(
                ToolFixture.arbitrary(
                    toolId,
                    2,
                    "name2",
                    ToolType.KEEP,
                    1,
                    IdleState.IDLE_HIGH,
                    true,
                    "https://wiki.example.com/tool1",
                    setOf(qualificationId2)
                ),
                ToolFixture.arbitrary(
                    toolId3,
                    2,
                    "newName3",
                    ToolType.KEEP,
                    3,
                    IdleState.IDLE_HIGH,
                    false,
                    "https://wiki.example.com/newtool3",
                    setOf(qualificationId2)
                )
            )
        }
    }

    @Nested
    internal inner class GivenToolsWithQualificationsStoredInRepository {
        private lateinit var repository: ToolRepository

        @BeforeEach
        fun setup() {
            repository = ToolDatabaseRepository()

            val tool1created = ToolCreated(
                toolId,
                actorId,
                correlationId,
                "tool1",
                ToolType.UNLOCK,
                1,
                IdleState.IDLE_HIGH,
                "https://wiki.example.com/tool1",
                setOf(qualificationId)
            )
            repository.store(tool1created)

            val tool2created = ToolCreated(
                toolId2,
                actorId,
                correlationId,
                "tool2",
                ToolType.KEEP,
                2,
                IdleState.IDLE_HIGH,
                "https://wiki.example.com/tool2",
                setOf(qualificationId, qualificationId2)
            )
            repository.store(tool2created)

            val tool3created = ToolCreated(
                toolId3,
                actorId,
                correlationId,
                "tool3",
                ToolType.KEEP,
                3,
                IdleState.IDLE_HIGH,
                "https://wiki.example.com/tool3",
                setOf(qualificationId2)
            )
            repository.store(tool3created)
        }

        @ParameterizedTest
        @MethodSource("cloud.fabX.fabXaccess.tool.infrastructure.ToolDatabaseRepositoryTest#toolAndQualificationIds")
        fun `when getting tools by qualification id then returns tools which require qualification`() {
            // given
            val repository = this.repository as GettingToolsByQualificationId

            // when
            val result = repository.getToolsByQualificationId(qualificationId)

            // then
            assertThat(result)
                .transform { it.map { tool -> tool.id } }
                .containsExactlyInAnyOrder(toolId, toolId2)
        }
    }
}