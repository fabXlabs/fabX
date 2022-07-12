package cloud.fabX.fabXaccess.qualification.infrastructure

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.qualification.model.QualificationCreated
import cloud.fabX.fabXaccess.qualification.model.QualificationDetailsChanged
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFixture
import cloud.fabX.fabXaccess.qualification.model.QualificationRepository
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import isLeft
import isNone
import isRight
import isSome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class QualificationDatabaseRepositoryTest {
    val qualificationId = QualificationIdFixture.staticId(123)
    val actorId = UserIdFixture.staticId(42)

    @Test
    fun `given empty repository when getting qualification by id then returns qualification not found error`() {
        // given
        val repository = QualificationDatabaseRepository()

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

        private var repository: QualificationRepository? = null

        @BeforeEach
        fun setup() {
            repository = QualificationDatabaseRepository()

            val event1 = QualificationCreated(
                qualificationId,
                actorId,
                name = "quali",
                description = "description",
                colour = "#000000",
                orderNr = 1
            )
            repository!!.store(event1)

            val event2 = QualificationDetailsChanged(
                qualificationId,
                2,
                actorId,
                name = ChangeableValue.LeaveAsIs,
                description = ChangeableValue.ChangeToValue("a good qualification"),
                colour = ChangeableValue.LeaveAsIs,
                orderNr = ChangeableValue.ChangeToValue(100)
            )
            repository!!.store(event2)
        }

        @Test
        fun `when getting qualification by id then returns qualification from events`() {
            // given
            val repository = this.repository!!

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
        fun `when storing then accepts aggregate version number increased by one`() {
            // given
            val repository = this.repository!!

            val event = QualificationDetailsChanged(
                qualificationId,
                3,
                actorId,
                ChangeableValue.LeaveAsIs,
                ChangeableValue.LeaveAsIs,
                ChangeableValue.LeaveAsIs,
                ChangeableValue.LeaveAsIs
            )

            // when
            val result = repository.store(event)

            // then
            assertThat(result).isNone()

            assertThat(repository.getById(qualificationId))
                .isRight()
                .transform { it.aggregateVersion }.isEqualTo(3)
        }

        @ParameterizedTest
        @ValueSource(longs = [-1, 0, 1, 2, 4, 100])
        fun `when storing then not accepts version numbers other than increased by one`(version: Long) {
            // given
            val repository = this.repository!!

            val event = QualificationDetailsChanged(
                qualificationId,
                version,
                actorId,
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
                        "Previous version of qualification QualificationId(value=3ec3cfae-43c1-3af8-8a4b-8cf636d21640) is 2, " +
                                "desired new version is $version."
                    )
                )

            assertThat(repository.getById(qualificationId))
                .isRight()
                .transform { it.aggregateVersion }.isEqualTo(2)
        }

    }

}