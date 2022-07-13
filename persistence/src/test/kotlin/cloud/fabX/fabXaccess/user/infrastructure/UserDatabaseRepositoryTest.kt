package cloud.fabX.fabXaccess.user.infrastructure

import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.user.model.UserCreated
import cloud.fabX.fabXaccess.user.model.UserDeleted
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import cloud.fabX.fabXaccess.user.model.UserLockStateChanged
import cloud.fabX.fabXaccess.user.model.UserPersonalInformationChanged
import cloud.fabX.fabXaccess.user.model.UserRepository
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
            .isEqualTo(
                Error.UserNotFound(
                    "User with id UserId(value=58de55f4-f3cd-3fde-8a2f-59b01c428779) not found.",
                    userId
                )
            )
    }

    @Nested
    internal inner class GivenEventsForUserStoredInRepository {

        private var repository: UserRepository? = null

        @BeforeEach
        fun setup() {
            repository = UserDatabaseRepository()

            val event1 = UserCreated(
                userId,
                actorId,
                firstName = "first",
                lastName = "last",
                wikiName = "wiki"
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

    @Nested
    internal inner class GivenEventsForUsersStoredInRepository {

        private val userId2 = UserIdFixture.staticId(12345)
        private val userId3 = UserIdFixture.staticId(123456)

        private var repository: UserRepository? = null

        @BeforeEach
        fun setup() {
            repository = UserDatabaseRepository()

            val user1event1 = UserCreated(
                userId,
                actorId,
                firstName = "first1",
                lastName = "last1",
                wikiName = "wiki1"
            )
            repository!!.store(user1event1)

            val user1event2 = UserLockStateChanged(
                userId,
                2,
                actorId,
                locked = ChangeableValue.ChangeToValue(true),
                notes = ChangeableValue.ChangeToValue("some notes")
            )
            repository!!.store(user1event2)

            val user2event1 = UserCreated(
                userId2,
                actorId,
                firstName = "first2",
                lastName = "last2",
                wikiName = "wiki2"
            )
            repository!!.store(user2event1)

            val user1event3 = UserPersonalInformationChanged(
                userId,
                3,
                actorId,
                firstName = ChangeableValue.ChangeToValue("first1v3"),
                lastName = ChangeableValue.LeaveAsIs,
                wikiName = ChangeableValue.LeaveAsIs
            )
            repository!!.store(user1event3)

            val user3event1 = UserCreated(
                userId3,
                actorId,
                firstName = "first3",
                lastName = "last3",
                wikiName = "wiki3"
            )
            repository!!.store(user3event1)

            val user2event2 = UserPersonalInformationChanged(
                userId2,
                2,
                actorId,
                firstName = ChangeableValue.ChangeToValue("first2v2"),
                lastName = ChangeableValue.ChangeToValue("last2v2"),
                wikiName = ChangeableValue.ChangeToValue("wiki2v2")
            )
            repository!!.store(user2event2)

            val user3event2 = UserDeleted(
                userId3,
                2,
                actorId
            )
            repository!!.store(user3event2)
        }

        @Test
        fun `when getting all users then returns all users from events`() {
            // given
            val repository = this.repository!!

            // when
            val result = repository.getAll()

            // then
            assertThat(result).containsExactlyInAnyOrder(
                UserFixture.arbitraryUser(
                    userId,
                    3,
                    "first1v3",
                    "last1",
                    "wiki1",
                    true,
                    "some notes",
                    listOf(),
                    null,
                    false
                ),
                UserFixture.arbitraryUser(
                    userId2,
                    2,
                    "first2v2",
                    "last2v2",
                    "wiki2v2",
                    false,
                    null,
                    listOf(),
                    null,
                    false
                )
            )
        }

    }
}