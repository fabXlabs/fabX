package cloud.fabX.fabXaccess.user.infrastructure

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.isTrue
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.user.model.UserCreated
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import cloud.fabX.fabXaccess.user.model.UserLockStateChanged
import isLeft
import isNone
import isRight
import isSome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class UserDatabaseRepositoryTest {
    val userId = UserIdFixture.staticId(1234)
    val actorId = UserIdFixture.staticId(1)

    @Test
    fun `given empty repository when getting user by id then returns user not found error`() {
        // given
        val repository = UserDatabaseRepository()

        // when
        val result = repository.getById(userId)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(Error.UserNotFound(
                "User with id UserId(value=58de55f4-f3cd-3fde-8a2f-59b01c428779) not found.",
                userId
            ))
    }

    @Nested
    internal inner class GivenEventsForUserStoredInRepository {

        private var repository: UserDatabaseRepository? = null

        @BeforeEach
        fun setup() {
            repository = UserDatabaseRepository()

            val event1 = UserCreated(
                userId,
                actorId,
                firstName = "first",
                lastName = "last",
                wikiName = "wiki",
                phoneNumber = null
            )
            repository!!.store(event1)

            val event2 = UserLockStateChanged(
                userId,
                2,
                actorId,
                locked = ChangeableValue.ChangeToValue(true),
                notes = ChangeableValue.ChangeToValue("some notes")
            )
            repository!!.store(event2)
        }

        @Test
        fun `when getting user by id then returns user from events`() {
            // given
            val repository = this.repository!!

            // when
            val result = repository.getById(userId)

            // then
            assertThat(result)
                .isRight()
                .all {
                    transform { it.id }.isEqualTo(userId)
                    transform { it.aggregateVersion }.isEqualTo(2)
                    transform { it.firstName }.isEqualTo("first")
                    transform { it.lastName }.isEqualTo("last")
                    transform { it.wikiName }.isEqualTo("wiki")
                    transform { it.phoneNumber }.isNull()
                    transform { it.locked }.isTrue()
                    transform { it.notes }.isEqualTo("some notes")
                }
        }

        @Test
        fun `when storing then accepts aggregate version number increased by one`() {
            // given
            val repository = this.repository!!

            val event = UserLockStateChanged(
                userId,
                3,
                actorId,
                locked = ChangeableValue.ChangeToValue(false),
                notes = ChangeableValue.ChangeToValue(null)
            )

            // when
            val result = repository.store(event)

            // then
            assertThat(result).isNone()

            assertThat(repository.getById(userId))
                .isRight()
                .transform { it.aggregateVersion }.isEqualTo(3)
        }

        @ParameterizedTest
        @ValueSource(longs = [-1, 0, 2, 4, 42])
        fun `when storing then not accepts version numbers other than increased by one`(
            version: Long
        ) {
            // given
            val repository = this.repository!!

            val event = UserLockStateChanged(
                userId,
                version,
                actorId,
                locked = ChangeableValue.ChangeToValue(false),
                notes = ChangeableValue.ChangeToValue(null)
            )

            // when
            val result = repository.store(event)

            // then
            assertThat(result)
                .isSome()
                .isEqualTo(
                    Error.VersionConflict(
                        "Previous version of user UserId(value=58de55f4-f3cd-3fde-8a2f-59b01c428779) is 2, " +
                                "desired new version is $version."
                    )
                )

            assertThat(repository.getById(userId))
                .isRight()
                .transform { it.aggregateVersion }.isEqualTo(2)
        }
    }
}