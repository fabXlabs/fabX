package cloud.fabX.fabXaccess.qualification.infrastructure

import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.each
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import cloud.fabX.fabXaccess.common.infrastructure.withTestApp
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.qualification.model.QualificationCreated
import cloud.fabX.fabXaccess.qualification.model.QualificationDeleted
import cloud.fabX.fabXaccess.qualification.model.QualificationDetailsChanged
import cloud.fabX.fabXaccess.qualification.model.QualificationFixture
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFixture
import cloud.fabX.fabXaccess.qualification.model.QualificationSourcingEvent
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import isLeft
import isRight
import kotlinx.datetime.Clock
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.kodein.di.instance

internal open class QualificationDatabaseRepositoryTest {
    private val qualificationId = QualificationIdFixture.static(123)
    private val actorId = UserIdFixture.static(42)
    private val correlationId = CorrelationIdFixture.arbitrary()
    private val fixedInstant = Clock.System.now()

    internal open fun withRepository(block: suspend (QualificationDatabaseRepository) -> Unit) = withTestApp { di ->
        val repository: QualificationDatabaseRepository by di.instance()
        block(repository)
    }

    @Test
    fun `given empty repository when getting qualification by id then returns qualification not found error`() =
        withRepository { repository ->
            // given

            // when
            val result = repository.getById(qualificationId)

            // then
            assertThat(result)
                .isLeft()
                .isEqualTo(
                    Error.QualificationNotFound(
                        "Qualification with id QualificationId(value=3ec3cfae-43c1-3af8-8a4b-8cf636d21640) not found.",
                        qualificationId
                    )
                )
        }

    @Nested
    internal inner class GivenEventsForQualificationStoredInRepository {

        private fun withSetupTestApp(block: suspend (QualificationDatabaseRepository) -> Unit) =
            withRepository { repository ->
                val event1 = QualificationCreated(
                    qualificationId,
                    actorId,
                    fixedInstant,
                    correlationId,
                    name = "quali",
                    description = "description",
                    colour = "#000000",
                    orderNr = 1
                )
                repository.store(event1)

                val event2 = QualificationDetailsChanged(
                    qualificationId,
                    2,
                    actorId,
                    fixedInstant,
                    correlationId,
                    name = ChangeableValue.LeaveAsIs,
                    description = ChangeableValue.ChangeToValueString("a good qualification"),
                    colour = ChangeableValue.LeaveAsIs,
                    orderNr = ChangeableValue.ChangeToValueInt(100)
                )
                repository.store(event2)

                block(repository)
            }

        @Test
        fun `when getting qualification by id then returns qualification from events`() =
            withSetupTestApp { repository ->
                // given

                // when
                val result = repository.getById(qualificationId)

                // then
                assertThat(result)
                    .isRight()
                    .all {
                        transform { it.id }.isEqualTo(qualificationId)
                        transform { it.aggregateVersion }.isEqualTo(2)
                        transform { it.name }.isEqualTo("quali")
                        transform { it.description }.isEqualTo("a good qualification")
                        transform { it.colour }.isEqualTo("#000000")
                        transform { it.orderNr }.isEqualTo(100)
                    }
            }

        @Test
        fun `when storing then accepts aggregate version number increased by one`() = withSetupTestApp { repository ->
            // given
            val event = QualificationDetailsChanged(
                qualificationId,
                3,
                actorId,
                fixedInstant,
                correlationId,
                ChangeableValue.LeaveAsIs,
                ChangeableValue.LeaveAsIs,
                ChangeableValue.LeaveAsIs,
                ChangeableValue.LeaveAsIs
            )

            // when
            val result = repository.store(event)

            // then
            assertThat(result)
                .isRight()
                .isEqualTo(Unit)

            assertThat(repository.getById(qualificationId))
                .isRight()
                .transform { it.aggregateVersion }.isEqualTo(3)
        }

        @ParameterizedTest
        @ValueSource(longs = [-1, 0, 1, 2, 4, 100])
        fun `when storing then not accepts version numbers other than increased by one`(version: Long) =
            withSetupTestApp { repository ->
                // given
                val event = QualificationDetailsChanged(
                    qualificationId,
                    version,
                    actorId,
                    fixedInstant,
                    correlationId,
                    ChangeableValue.LeaveAsIs,
                    ChangeableValue.LeaveAsIs,
                    ChangeableValue.LeaveAsIs,
                    ChangeableValue.LeaveAsIs
                )

                // when
                val result = repository.store(event)

                // then
                assertThat(result)
                    .isLeft()
                    .isEqualTo(
                        Error.VersionConflict(
                            "Previous version of qualification QualificationId(value=3ec3cfae-43c1-3af8-8a4b-8cf636d21640) is 2, " +
                                    "desired new version is $version."
                        )
                    )

                assertThat(repository.getById(qualificationId))
                    .isRight()
                    .transform { it.aggregateVersion }.isEqualTo(2)
            }
    }

    @Nested
    internal inner class GivenEventsForQualificationsStoredInRepository {

        private val qualificationId2 = QualificationIdFixture.static(234)
        private val qualificationId3 = QualificationIdFixture.static(345)

        private fun withSetupTestApp(block: suspend (QualificationDatabaseRepository) -> Unit) =
            withRepository { repository ->
                val qualification1event1 = QualificationCreated(
                    qualificationId,
                    actorId,
                    fixedInstant,
                    correlationId,
                    name = "qualification1",
                    description = "description1",
                    colour = "#000001",
                    orderNr = 1
                )
                repository.store(qualification1event1)

                val qualification1event2 = QualificationDetailsChanged(
                    qualificationId,
                    2,
                    actorId,
                    fixedInstant,
                    correlationId,
                    name = ChangeableValue.LeaveAsIs,
                    description = ChangeableValue.ChangeToValueString("description1v2"),
                    colour = ChangeableValue.LeaveAsIs,
                    orderNr = ChangeableValue.LeaveAsIs
                )
                repository.store(qualification1event2)

                val qualification2event1 = QualificationCreated(
                    qualificationId2,
                    actorId,
                    fixedInstant,
                    correlationId,
                    name = "qualification2",
                    description = "description2",
                    colour = "#000002",
                    orderNr = 2
                )
                repository.store(qualification2event1)

                val qualification3event1 = QualificationCreated(
                    qualificationId3,
                    actorId,
                    fixedInstant,
                    correlationId,
                    name = "qualification3",
                    description = "description3",
                    colour = "#000003",
                    orderNr = 3
                )
                repository.store(qualification3event1)

                val qualification2event2 = QualificationDeleted(
                    qualificationId2,
                    2,
                    actorId,
                    fixedInstant,
                    correlationId
                )
                repository.store(qualification2event2)

                val qualification3event2 = QualificationDetailsChanged(
                    qualificationId3,
                    2,
                    actorId,
                    fixedInstant,
                    correlationId,
                    name = ChangeableValue.LeaveAsIs,
                    description = ChangeableValue.ChangeToValueString("description3v2"),
                    colour = ChangeableValue.ChangeToValueString("#333333"),
                    orderNr = ChangeableValue.LeaveAsIs
                )
                repository.store(qualification3event2)

                block(repository)
            }

        @Test
        fun `when getting all qualifications then returns all qualifications from events`() =
            withSetupTestApp { repository ->
                // given

                // when
                val result = repository.getAll()

                // then
                assertThat(result).containsExactlyInAnyOrder(
                    QualificationFixture.arbitrary(
                        qualificationId,
                        2,
                        "qualification1",
                        "description1v2",
                        "#000001",
                        1
                    ),
                    QualificationFixture.arbitrary(
                        qualificationId3,
                        2,
                        "qualification3",
                        "description3v2",
                        "#333333",
                        3
                    ),
                )
            }

        @Test
        fun `when getting sourcing events then returns sourcing events`() = withSetupTestApp { repository ->
            // given

            // when
            val result = repository.getSourcingEvents()

            // then
            assertThat(result).all {
                hasSize(6)
                each {
                    it.isInstanceOf(QualificationSourcingEvent::class)
                }
            }
        }
    }
}