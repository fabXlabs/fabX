package cloud.fabX.fabXaccess.qualification.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import cloud.fabX.fabXaccess.common.model.AggregateVersionDoesNotIncreaseOneByOne
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.IterableIsEmpty
import cloud.fabX.fabXaccess.tool.model.GettingToolsByQualificationId
import cloud.fabX.fabXaccess.user.model.AdminFixture
import isNone
import isRight
import isSome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class QualificationTest {

    private val adminActor = AdminFixture.arbitrary()
    private val correlationId = CorrelationIdFixture.arbitrary()

    private val qualificationId = QualificationIdFixture.arbitrary()
    private val aggregateVersion = 123L

    @Test
    fun `given valid values when constructing qualification then it is constructed`() {
        // given

        // when
        val qualification = Qualification(
            qualificationId,
            aggregateVersion,
            "Door Shop",
            "Door of the workshop",
            "#ff40ff",
            50
        )

        // then
        assertThat(qualification).isNotNull()
        assertThat(qualification.id).isEqualTo(qualificationId)
        assertThat(qualification.aggregateVersion).isEqualTo(aggregateVersion)
    }

    @Test
    fun `when adding new qualification then returns expected souring event`() {
        // given
        val name = "name"
        val description = "description"
        val colour = "#123456"
        val orderNr = 123

        val expectedSourcingEvent = QualificationCreated(
            qualificationId,
            adminActor.id,
            correlationId,
            name,
            description,
            colour,
            orderNr
        )

        // when
        val result = Qualification.addNew(
            { qualificationId },
            adminActor,
            correlationId,
            name,
            description,
            colour,
            orderNr
        )

        // then
        assertThat(result).isEqualTo(expectedSourcingEvent)
    }

    @Test
    fun `given no sourcing events when constructing qualification from sourcing events then throws exception`() {
        // given

        // when
        val exception = assertThrows<IterableIsEmpty> {
            Qualification.fromSourcingEvents(listOf())
        }

        // then
        assertThat(exception.message)
            .isEqualTo("No sourcing events contained in iterable.")
    }

    @Test
    fun `given no QualificationCreated event when constructing qualification from sourcing events then throws exception`() {
        // given
        val event = QualificationDetailsChanged(
            qualificationId,
            1,
            adminActor.id,
            CorrelationIdFixture.arbitrary(),
            name = ChangeableValue.ChangeToValueString("quali"),
            description = ChangeableValue.ChangeToValueString("quali description"),
            colour = ChangeableValue.ChangeToValueString("#000000"),
            orderNr = ChangeableValue.ChangeToValueInt(42)
        )

        // when
        val exception = assertThrows<Qualification.EventHistoryDoesNotStartWithQualificationCreated> {
            Qualification.fromSourcingEvents(listOf(event))
        }

        // then
        assertThat(exception.message)
            .isNotNull()
            .isEqualTo("Event history starts with $event, not a QualificationCreated event.")
    }

    @Test
    fun `given multiple in-order sourcing events when constructing qualification then applies all`() {
        // given
        val event1 = QualificationCreated(
            qualificationId,
            adminActor.id,
            CorrelationIdFixture.arbitrary(),
            "name1",
            "description1",
            "#000001",
            1
        )
        val event2 = QualificationDetailsChanged(
            qualificationId,
            2,
            adminActor.id,
            CorrelationIdFixture.arbitrary(),
            ChangeableValue.ChangeToValueString("name2"),
            ChangeableValue.LeaveAsIs,
            ChangeableValue.ChangeToValueString("#000002"),
            ChangeableValue.LeaveAsIs
        )
        val event3 = QualificationDetailsChanged(
            qualificationId,
            3,
            adminActor.id,
            CorrelationIdFixture.arbitrary(),
            ChangeableValue.LeaveAsIs,
            ChangeableValue.ChangeToValueString("description3"),
            ChangeableValue.ChangeToValueString("#000003"),
            ChangeableValue.LeaveAsIs
        )

        // when
        val result = Qualification.fromSourcingEvents(listOf(event1, event2, event3))

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(
                Qualification(
                    qualificationId,
                    3,
                    "name2",
                    "description3",
                    "#000003",
                    1
                )
            )
    }

    @Test
    fun `given multiple out-of-order sourcing events when constructing qualification then throws exception`() {
        // given
        val event1 = QualificationCreated(
            qualificationId,
            adminActor.id,
            CorrelationIdFixture.arbitrary(),
            "name1",
            "description1",
            "#000001",
            1
        )
        val event3 = QualificationDetailsChanged(
            qualificationId,
            3,
            adminActor.id,
            CorrelationIdFixture.arbitrary(),
            ChangeableValue.LeaveAsIs,
            ChangeableValue.ChangeToValueString("description3"),
            ChangeableValue.ChangeToValueString("#000003"),
            ChangeableValue.LeaveAsIs
        )
        val event2 = QualificationDetailsChanged(
            qualificationId,
            2,
            adminActor.id,
            CorrelationIdFixture.arbitrary(),
            ChangeableValue.ChangeToValueString("name2"),
            ChangeableValue.LeaveAsIs,
            ChangeableValue.ChangeToValueString("#000002"),
            ChangeableValue.LeaveAsIs
        )

        // when
        val exception = assertThrows<AggregateVersionDoesNotIncreaseOneByOne> {
            Qualification.fromSourcingEvents(listOf(event1, event3, event2))
        }

        // then
        assertThat(exception.message)
            .isNotNull()
            .isEqualTo("Aggregate version does not increase one by one for ${listOf(event1, event3, event2)}.")
    }

    @Test
    fun `given sourcing events including QualificationDeleted event when constructing qualification then returns None`() {
        // given
        val event1 = QualificationCreated(
            qualificationId,
            adminActor.id,
            CorrelationIdFixture.arbitrary(),
            "name1",
            "description1",
            "#000001",
            1
        )

        val event2 = QualificationDeleted(
            qualificationId,
            2,
            adminActor.id,
            CorrelationIdFixture.arbitrary(),
        )

        // when
        val result = Qualification.fromSourcingEvents(listOf(event1, event2))

        // then
        assertThat(result)
            .isNone()
    }

    @Test
    fun `when changing details then expected sourcing event is returned`() {
        // given
        val qualification =
            QualificationFixture.arbitrary(qualificationId, aggregateVersion = aggregateVersion)

        val expectedSourcingEvent = QualificationDetailsChanged(
            aggregateRootId = qualificationId,
            aggregateVersion = aggregateVersion + 1,
            actorId = adminActor.id,
            correlationId = correlationId,
            name = ChangeableValue.ChangeToValueString("newName"),
            description = ChangeableValue.LeaveAsIs,
            colour = ChangeableValue.ChangeToValueString("#000042"),
            orderNr = ChangeableValue.LeaveAsIs
        )

        // when
        val result = qualification.changeDetails(
            adminActor,
            correlationId,
            name = ChangeableValue.ChangeToValueString("newName"),
            description = ChangeableValue.LeaveAsIs,
            colour = ChangeableValue.ChangeToValueString("#000042"),
            orderNr = ChangeableValue.LeaveAsIs
        )

        // then
        assertThat(result).isEqualTo(expectedSourcingEvent)
    }

    @Test
    fun `when deleting then expected sourcing event is returned`() {
        // given
        val qualification =
            QualificationFixture.arbitrary(qualificationId, aggregateVersion = aggregateVersion)

        val expectedSourcingEvent = QualificationDeleted(
            aggregateRootId = qualificationId,
            aggregateVersion = aggregateVersion + 1,
            actorId = adminActor.id,
            correlationId = correlationId
        )

        val gettingToolsByQualificationId = GettingToolsByQualificationId { setOf() }

        // when
        val result = qualification.delete(adminActor, correlationId, gettingToolsByQualificationId)

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(expectedSourcingEvent)
    }

    @Test
    fun `given valid qualification when stringifying then result is correct`() {
        // given
        val qualification = Qualification(
            QualificationIdFixture.static(1234),
            aggregateVersion,
            "Door Shop",
            "Door of the workshop",
            "#ff40ff",
            50
        )

        // when
        val result = qualification.toString()

        // then
        assertThat(result).isEqualTo(
            "Qualification(id=QualificationId(value=58de55f4-f3cd-3fde-8a2f-59b01c428779), " +
                    "aggregateVersion=123, " +
                    "name=Door Shop, " +
                    "description=Door of the workshop, " +
                    "colour=#ff40ff, " +
                    "orderNr=50)"
        )
    }
}
