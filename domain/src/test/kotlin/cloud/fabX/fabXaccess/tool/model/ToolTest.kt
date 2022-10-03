package cloud.fabX.fabXaccess.tool.model

import FixedClock
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import cloud.fabX.fabXaccess.common.model.AggregateVersionDoesNotIncreaseOneByOne
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.IterableIsEmpty
import cloud.fabX.fabXaccess.qualification.model.GettingQualificationById
import cloud.fabX.fabXaccess.qualification.model.QualificationFixture
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFixture
import cloud.fabX.fabXaccess.user.model.AdminFixture
import isLeft
import isNone
import isRight
import isSome
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@OptIn(ExperimentalCoroutinesApi::class)
internal class ToolTest {

    private val adminActor = AdminFixture.arbitrary()
    private val correlationId = CorrelationIdFixture.arbitrary()

    private val fixedInstant = Clock.System.now()
    private val fixedClock = FixedClock(fixedInstant)

    private val toolId = ToolIdFixture.arbitrary()
    private val aggregateVersion = 42L

    @Test
    fun `given valid values when constructing tool then it is constructed`() {
        // given
        val qualificationId = QualificationIdFixture.arbitrary()

        // when
        val tool = Tool(
            toolId,
            aggregateVersion,
            "Door Shop",
            ToolType.UNLOCK,
            true,
            200,
            IdleState.IDLE_HIGH,
            true,
            "https://example.com/shopdoor",
            requiredQualifications = setOf(qualificationId)
        )

        // then
        assertThat(tool).isNotNull()
        assertThat(tool.id).isEqualTo(toolId)
        assertThat(tool.aggregateVersion).isEqualTo(aggregateVersion)
    }

    @Test
    fun `when adding new tool then returns expected sourcing event`() = runTest {
        // given
        val qualificationId1 = QualificationIdFixture.arbitrary()
        val qualification1 = QualificationFixture.arbitrary(qualificationId1)

        val qualificationId2 = QualificationIdFixture.arbitrary()
        val qualification2 = QualificationFixture.arbitrary(qualificationId2)

        val name = "name"
        val type = ToolType.KEEP
        val requires2FA = true
        val time = 42
        val idleState = IdleState.IDLE_LOW
        val wikiLink = "https://example.com/tool42"
        val requiredQualifications = setOf(qualificationId1, qualificationId2)

        val expectedSourcingEvent = ToolCreated(
            toolId,
            adminActor.id,
            fixedInstant,
            correlationId,
            name,
            type,
            requires2FA,
            time,
            idleState,
            wikiLink,
            requiredQualifications
        )

        // when
        val result = Tool.addNew(
            { toolId },
            adminActor,
            fixedClock,
            correlationId,
            name,
            type,
            requires2FA,
            time,
            idleState,
            wikiLink,
            requiredQualifications,
            GettingQualificationById {
                return@GettingQualificationById when (it) {
                    qualificationId1 -> qualification1.right()
                    qualificationId2 -> qualification2.right()
                    else -> throw IllegalArgumentException("unexpected qualification id")
                }
            }
        )

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(expectedSourcingEvent)
    }

    @Test
    fun `given invalid qualification id when adding new tool then returns error`() = runTest {
        // given
        val invalidQualificationId = QualificationIdFixture.arbitrary()

        val error = Error.QualificationNotFound("msg", invalidQualificationId)

        // when
        val result = Tool.addNew(
            { toolId },
            adminActor,
            fixedClock,
            correlationId,
            "tool",
            ToolType.UNLOCK,
            false,
            123,
            IdleState.IDLE_LOW,
            "https://example.com/tool",
            setOf(invalidQualificationId),
            GettingQualificationById {
                return@GettingQualificationById when (it) {
                    invalidQualificationId -> error.left()
                    else -> throw IllegalArgumentException("unexpected qualification id")
                }
            }
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.ReferencedQualificationNotFound(
                    "msg",
                    invalidQualificationId,
                    correlationId
                )
            )
    }

    @Test
    fun `given no sourcing events when constructing tool from sourcing events then throws exception`() {
        // given

        // when
        val exception = assertThrows<IterableIsEmpty> {
            Tool.fromSourcingEvents(listOf())
        }

        // then
        assertThat(exception.message)
            .isEqualTo("No sourcing events contained in iterable.")
    }

    @Test
    fun `given no ToolCreated event when constructing tool from sourcing events then throws exception`() {
        // given
        val event = ToolDetailsChanged(
            toolId,
            1,
            adminActor.id,
            fixedInstant,
            CorrelationIdFixture.arbitrary(),
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs,
            ChangeableValue.LeaveAsIs
        )

        // when
        val exception = assertThrows<Tool.EventHistoryDoesNotStartWithToolCreated> {
            Tool.fromSourcingEvents(listOf(event))
        }

        // then
        assertThat(exception.message)
            .isNotNull()
            .isEqualTo("Event history starts with $event, not a ToolCreated event.")
    }

    @Test
    fun `given multiple in-order sourcing events when constructing tool then applies all`() {
        // given
        val qualificationId1 = QualificationIdFixture.arbitrary()
        val qualificationId2 = QualificationIdFixture.arbitrary()

        val event1 = ToolCreated(
            toolId,
            adminActor.id,
            fixedInstant,
            CorrelationIdFixture.arbitrary(),
            "name1",
            ToolType.UNLOCK,
            true,
            1,
            IdleState.IDLE_LOW,
            "https://example.com/1",
            setOf(qualificationId1)
        )

        val event2 = ToolDetailsChanged(
            toolId,
            2,
            adminActor.id,
            fixedInstant,
            CorrelationIdFixture.arbitrary(),
            name = ChangeableValue.ChangeToValueString("name2"),
            toolType = ChangeableValue.LeaveAsIs,
            time = ChangeableValue.ChangeToValueInt(2),
            idleState = ChangeableValue.LeaveAsIs,
            enabled = ChangeableValue.LeaveAsIs,
            wikiLink = ChangeableValue.LeaveAsIs,
            requiredQualifications = ChangeableValue.ChangeToValueQualificationSet(
                setOf(
                    qualificationId1,
                    qualificationId2
                )
            )
        )

        val event3 = ToolDetailsChanged(
            toolId,
            3,
            adminActor.id,
            fixedInstant,
            CorrelationIdFixture.arbitrary(),
            name = ChangeableValue.ChangeToValueString("name3"),
            toolType = ChangeableValue.ChangeToValueToolType(ToolType.KEEP),
            time = ChangeableValue.LeaveAsIs,
            idleState = ChangeableValue.LeaveAsIs,
            enabled = ChangeableValue.ChangeToValueBoolean(false),
            wikiLink = ChangeableValue.ChangeToValueString("https://example.com/3"),
            requiredQualifications = ChangeableValue.ChangeToValueQualificationSet(
                setOf(
                    qualificationId1,
                    qualificationId2
                )
            )
        )

        // when
        val result = Tool.fromSourcingEvents(listOf(event1, event2, event3))

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(
                Tool(
                    toolId,
                    3,
                    "name3",
                    ToolType.KEEP,
                    true,
                    2,
                    IdleState.IDLE_LOW,
                    false,
                    "https://example.com/3",
                    setOf(qualificationId1, qualificationId2)
                )
            )
    }

    @Test
    fun `given multiple out-of-order sourcing events when constructing tool then throws exception`() {
        // given
        val qualificationId1 = QualificationIdFixture.arbitrary()
        val qualificationId2 = QualificationIdFixture.arbitrary()

        val event1 = ToolCreated(
            toolId,
            adminActor.id,
            fixedInstant,
            CorrelationIdFixture.arbitrary(),
            "name1",
            ToolType.UNLOCK,
            false,
            1,
            IdleState.IDLE_LOW,
            "https://example.com/1",
            setOf(qualificationId1)
        )

        val event3 = ToolDetailsChanged(
            toolId,
            3,
            adminActor.id,
            fixedInstant,
            CorrelationIdFixture.arbitrary(),
            name = ChangeableValue.ChangeToValueString("name3"),
            toolType = ChangeableValue.LeaveAsIs,
            time = ChangeableValue.ChangeToValueInt(3),
            idleState = ChangeableValue.LeaveAsIs,
            enabled = ChangeableValue.LeaveAsIs,
            wikiLink = ChangeableValue.LeaveAsIs,
            requiredQualifications = ChangeableValue.ChangeToValueQualificationSet(
                setOf(
                    qualificationId1,
                    qualificationId2
                )
            )
        )

        val event2 = ToolDetailsChanged(
            toolId,
            2,
            adminActor.id,
            fixedInstant,
            CorrelationIdFixture.arbitrary(),
            name = ChangeableValue.ChangeToValueString("name2"),
            toolType = ChangeableValue.ChangeToValueToolType(ToolType.KEEP),
            time = ChangeableValue.LeaveAsIs,
            idleState = ChangeableValue.LeaveAsIs,
            enabled = ChangeableValue.ChangeToValueBoolean(false),
            wikiLink = ChangeableValue.ChangeToValueString("https://example.com/2"),
            requiredQualifications = ChangeableValue.ChangeToValueQualificationSet(
                setOf(
                    qualificationId1,
                    qualificationId2
                )
            )
        )

        // when
        val exception = assertThrows<AggregateVersionDoesNotIncreaseOneByOne> {
            Tool.fromSourcingEvents(listOf(event1, event3, event2))
        }

        // then
        assertThat(exception.message)
            .isNotNull()
            .isEqualTo("Aggregate version does not increase one by one for ${listOf(event1, event3, event2)}.")
    }

    @Test
    fun `given sourcing events including ToolDeleted event when constructing tool then returns None`() {
        // given
        val event1 = ToolCreated(
            toolId,
            adminActor.id,
            fixedInstant,
            CorrelationIdFixture.arbitrary(),
            "name1",
            ToolType.UNLOCK,
            true,
            1,
            IdleState.IDLE_LOW,
            "https://example.com/1",
            setOf()
        )

        val event2 = ToolDeleted(
            toolId,
            2,
            adminActor.id,
            fixedInstant,
            CorrelationIdFixture.arbitrary()
        )

        // when
        val result = Tool.fromSourcingEvents(listOf(event1, event2))

        // then
        assertThat(result)
            .isNone()
    }

    @Test
    fun `when changing details then expected sourcing event is returned`() = runTest {
        // given
        val tool = ToolFixture.arbitrary(toolId, aggregateVersion = aggregateVersion)

        val qualificationId = QualificationIdFixture.arbitrary()
        val qualification = QualificationFixture.arbitrary(qualificationId)

        val expectedSourcingEvent = ToolDetailsChanged(
            toolId,
            aggregateVersion + 1,
            adminActor.id,
            fixedInstant,
            correlationId,
            name = ChangeableValue.ChangeToValueString("newName"),
            toolType = ChangeableValue.LeaveAsIs,
            time = ChangeableValue.ChangeToValueInt(9876),
            idleState = ChangeableValue.ChangeToValueIdleState(IdleState.IDLE_LOW),
            enabled = ChangeableValue.LeaveAsIs,
            wikiLink = ChangeableValue.ChangeToValueString("https://example.com/newLink"),
            requiredQualifications = ChangeableValue.ChangeToValueQualificationSet(setOf(qualificationId))
        )

        // when
        val result = tool.changeDetails(
            adminActor,
            fixedClock,
            correlationId,
            name = ChangeableValue.ChangeToValueString("newName"),
            type = ChangeableValue.LeaveAsIs,
            time = ChangeableValue.ChangeToValueInt(9876),
            idleState = ChangeableValue.ChangeToValueIdleState(IdleState.IDLE_LOW),
            enabled = ChangeableValue.LeaveAsIs,
            wikiLink = ChangeableValue.ChangeToValueString("https://example.com/newLink"),
            requiredQualifications = ChangeableValue.ChangeToValueQualificationSet(setOf(qualificationId)),
            GettingQualificationById {
                return@GettingQualificationById when (it) {
                    qualificationId -> qualification.right()
                    else -> throw IllegalArgumentException("unexpected qualification id")
                }
            }
        )

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(expectedSourcingEvent)
    }

    @Test
    fun `given invalid qualification id when changing details then returns error`() = runTest {
        // given
        val invalidQualificationId = QualificationIdFixture.arbitrary()
        val error = Error.QualificationNotFound("msg", invalidQualificationId)

        val tool = ToolFixture.arbitrary(toolId, aggregateVersion = aggregateVersion)

        // when
        val result = tool.changeDetails(
            adminActor,
            fixedClock,
            correlationId,
            name = ChangeableValue.ChangeToValueString("newName"),
            type = ChangeableValue.LeaveAsIs,
            time = ChangeableValue.ChangeToValueInt(9876),
            idleState = ChangeableValue.ChangeToValueIdleState(IdleState.IDLE_LOW),
            enabled = ChangeableValue.LeaveAsIs,
            wikiLink = ChangeableValue.ChangeToValueString("https://example.com/newLink"),
            requiredQualifications = ChangeableValue.ChangeToValueQualificationSet(setOf(invalidQualificationId)),
            GettingQualificationById {
                return@GettingQualificationById when (it) {
                    invalidQualificationId -> error.left()
                    else -> throw IllegalArgumentException("unexpected qualification id")
                }
            }
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.ReferencedQualificationNotFound(
                    "msg",
                    invalidQualificationId,
                    correlationId
                )
            )
    }

    @Test
    fun `when deleting then expected sourcing event is returned`() {
        // given
        val tool = ToolFixture.arbitrary(toolId, aggregateVersion = aggregateVersion)

        val expectedSourcingEvent = ToolDeleted(
            toolId,
            aggregateVersion + 1,
            adminActor.id,
            fixedInstant,
            correlationId
        )

        // when
        val result = tool.delete(adminActor, fixedClock, correlationId)

        // then
        assertThat(result).isEqualTo(expectedSourcingEvent)
    }

    @Test
    fun `given valid tool when stringifying then result is correct`() {
        // given
        val tool = Tool(
            ToolIdFixture.static(456),
            aggregateVersion,
            "Door Shop",
            ToolType.UNLOCK,
            true,
            200,
            IdleState.IDLE_HIGH,
            true,
            "https://example.com/shopdoor",
            requiredQualifications = setOf(QualificationIdFixture.static(678))
        )

        // when
        val result = tool.toString()

        // then
        assertThat(result).isEqualTo(
            "Tool(id=ToolId(value=31174c35-800c-3c91-bf56-08f55101fcfc), " +
                    "aggregateVersion=42, " +
                    "name=Door Shop, " +
                    "type=UNLOCK, " +
                    "requires2FA=true, " +
                    "time=200, " +
                    "idleState=IDLE_HIGH, " +
                    "enabled=true, " +
                    "wikiLink=https://example.com/shopdoor, " +
                    "requiredQualifications=[QualificationId(value=4bfb3604-f1ae-3404-b651-12239513f529)])"
        )
    }
}