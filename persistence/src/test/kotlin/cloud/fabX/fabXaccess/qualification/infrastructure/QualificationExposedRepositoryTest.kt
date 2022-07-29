package cloud.fabX.fabXaccess.qualification.infrastructure

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.infrastructure.withTestApp
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.module
import cloud.fabX.fabXaccess.qualification.model.QualificationCreated
import cloud.fabX.fabXaccess.qualification.model.QualificationDeleted
import cloud.fabX.fabXaccess.qualification.model.QualificationFixture
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFixture
import cloud.fabX.fabXaccess.qualification.model.QualificationSourcingEvent
import cloud.fabX.fabXaccess.user.model.AdminFixture
import isNone
import isRight
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.kodein.di.bindInstance
import org.kodein.di.instance

internal class QualificationExposedRepositoryTest {
    private val qualificationId1 = QualificationIdFixture.arbitrary()
    private val qualificationId2 = QualificationIdFixture.arbitrary()
    private val adminActor = AdminFixture.arbitrary()

    @Test
    fun `serializer test`() {
        val json = Json { serializersModule = module }

        val event1 = QualificationCreated(
            qualificationId1,
            adminActor.id,
            CorrelationIdFixture.arbitrary(),
            "name1",
            "description1",
            "#000001",
            1
        )

        val s = json.encodeToString(QualificationSourcingEvent.serializer(), event1)
        println(s)

        val d = json.decodeFromString(QualificationSourcingEvent.serializer(), s)

        // always have to serialize and deserialize as the SAME TYPE
        // i.e. the follow DOES NOT work
        // val d = json.decodeFromString(QualificationCreated.serializer(), s)

        println(d)
        assertThat(d).isEqualTo(event1)
    }

    @Test
    fun `given some inserts when get then returns inserted elements`() = withTestApp({
        bindInstance(tag = "dburl") { "jdbc:postgresql://localhost/postgres" }
        bindInstance(tag = "dbdriver") { "org.postgresql.Driver" }
        bindInstance(tag = "dbuser") { "postgres" }
        bindInstance(tag = "dbpassword") { "postgrespassword" }
    }) { di ->
        // given
        val testee: QualificationExposedRepository by di.instance()

        val event1 = QualificationCreated(
            qualificationId1,
            adminActor.id,
            CorrelationIdFixture.arbitrary(),
            "name1",
            "description1",
            "#000001",
            1
        )
        val event2 = QualificationDeleted(
            qualificationId1,
            2,
            adminActor.id,
            CorrelationIdFixture.arbitrary(),
        )
        val event3 = QualificationCreated(
            qualificationId2,
            adminActor.id,
            CorrelationIdFixture.arbitrary(),
            "name2",
            "description2",
            "#000002",
            2
        )

        val i1 = testee.store(event1)
        val i2 = testee.store(event2)
        val i3 = testee.store(event3)
        assertThat(i1).isNone()
        assertThat(i2).isNone()
        assertThat(i3).isNone()

        // when
        val result = testee.getSourcingEvents()

        // then
        assertThat(result)
            .containsExactly(
                event1,
                event2,
                event3
            )
    }

    @Test
    fun `given some inserts when get by id then returns qualification`() = withTestApp({
        bindInstance(tag = "dburl") { "jdbc:postgresql://localhost/postgres" }
        bindInstance(tag = "dbdriver") { "org.postgresql.Driver" }
        bindInstance(tag = "dbuser") { "postgres" }
        bindInstance(tag = "dbpassword") { "postgrespassword" }
    }) { di ->
        // given
        val testee: QualificationExposedRepository by di.instance()

        val event1 = QualificationCreated(
            qualificationId1,
            adminActor.id,
            CorrelationIdFixture.arbitrary(),
            "name1",
            "description1",
            "#000001",
            1
        )

        val i1 = testee.store(event1)
        assertThat(i1).isNone()

        // when
        val result = testee.getById(qualificationId1)

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(
                QualificationFixture.arbitrary(
                    qualificationId1,
                    1,
                    "name1",
                    "description1",
                    "#000001",
                    1
                )
            )
    }
}