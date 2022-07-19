package cloud.fabX.fabXaccess.user.infrastructure

import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFixture
import cloud.fabX.fabXaccess.user.model.*
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
    val userId = UserIdFixture.static(1234)
    val actorId = UserIdFixture.static(1)

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

        private val userId2 = UserIdFixture.static(12345)
        private val userId3 = UserIdFixture.static(123456)

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
                UserFixture.arbitrary(
                    userId,
                    3,
                    "first1v3",
                    "last1",
                    "wiki1",
                    true,
                    "some notes",
                    setOf(),
                    setOf(),
                    null,
                    false
                ),
                UserFixture.arbitrary(
                    userId2,
                    2,
                    "first2v2",
                    "last2v2",
                    "wiki2v2",
                    false,
                    null,
                    setOf(),
                    setOf(),
                    null,
                    false
                )
            )
        }

        @Test
        fun `when getting by known wiki name then returns user`() {
            // given
            val repository = this.repository!! as GettingUserByWikiName

            // when
            val result = repository.getByWikiName("wiki2v2")

            // then
            assertThat(result)
                .isRight()
                .transform { it.id }.isEqualTo(userId2)
        }

        @Test
        fun `when getting by unknown wiki name then returns error`() {
            // given
            val repository = this.repository!! as GettingUserByWikiName

            // when
            val result = repository.getByWikiName("unknownWikiName")

            // then
            assertThat(result)
                .isLeft()
                .isEqualTo(
                    Error.UserNotFoundByWikiName("Not able to find user for given wiki name.")
                )
        }
    }

    @Nested
    internal inner class GivenUsersWithIdentitiesStoredInRepository {

        private val userId2 = UserIdFixture.static(12345)

        private var repository: UserDatabaseRepository? = null

        @BeforeEach
        fun setup() {
            repository = UserDatabaseRepository()

            val user1Created = UserCreated(
                userId,
                actorId,
                firstName = "first1",
                lastName = "last1",
                wikiName = "wiki1"
            )
            repository!!.store(user1Created)

            val user1IdentityAdded = UsernamePasswordIdentityAdded(
                userId,
                2,
                actorId,
                "username1",
                "FyGrfqsvzCwU8UtVqZUI4MQ3pp3TTsOF6J//QLdSEoE="
            )
            repository!!.store(user1IdentityAdded)

            val user1CardIdentityAdded = CardIdentityAdded(
                userId,
                3,
                actorId,
                "11223344556677",
                "2312D5DFD79E5AA85BD0F43B565665BA3CEFAFF60689ACF8F49A7FADA0004756"
            )
            repository!!.store(user1CardIdentityAdded)

            val user2Created = UserCreated(
                userId2,
                actorId,
                firstName = "first2",
                lastName = "last2",
                wikiName = "wiki2"
            )
            repository!!.store(user2Created)

            val user2IdentityAdded = UsernamePasswordIdentityAdded(
                userId2,
                2,
                actorId,
                "username2",
                "Fp6cwyJURizWnWI2yWSsgg3FfrFErl/+vvkgdWsBdH8="
            )
            repository!!.store(user2IdentityAdded)

            val user2CardIdentityAdded = CardIdentityAdded(
                userId2,
                3,
                actorId,
                "AA11BB22CC33DD",
                "F4B726CC27C2413227382ABF095D09B1A13B00FC6AD1B1B5D75C4A954628C807"
            )
            repository!!.store(user2CardIdentityAdded)
        }

        @Test
        fun `when getting by known identity then returns user`() {
            // given
            val repository = this.repository!! as GettingUserByIdentity

            // when
            val result = repository.getByIdentity(
                UserIdentityFixture.usernamePassword("username1", "FyGrfqsvzCwU8UtVqZUI4MQ3pp3TTsOF6J//QLdSEoE=")
            )

            // then
            assertThat(result)
                .isRight()
                .transform { it.id }.isEqualTo(userId)
        }

        @Test
        fun `when getting by unknown identity then returns error`() {
            // given
            val repository = this.repository!! as GettingUserByIdentity

            // when
            val result = repository.getByIdentity(
                UserIdentityFixture.usernamePassword("unknownusername", "7pY1AYwQwy95R5xVtNRgen3+hYyEe+2gi+KxK2P64WY=")
            )

            // then
            assertThat(result)
                .isLeft()
                .isEqualTo(
                    Error.UserNotFoundByIdentity("Not able to find user for given identity.")
                )
        }

        @Test
        fun `when getting by known username then returns user`() {
            // given
            val repository = this.repository!! as GettingUserByUsername

            // when
            val result = repository.getByUsername("username1")

            // then
            assertThat(result)
                .isRight()
                .transform { it.id }.isEqualTo(userId)
        }

        @Test
        fun `when getting by unknown username then returns error`() {
            // given
            val repository = this.repository!! as GettingUserByUsername

            // when
            val result = repository.getByUsername("unknownusername")

            // then
            assertThat(result)
                .isLeft()
                .isEqualTo(
                    Error.UserNotFoundByUsername("Not able to find user for given username.")
                )
        }

        @Test
        fun `when getting by known card id then returns user`() {
            // given
            val repository = this.repository!! as GettingUserByCardId

            // when
            val result = repository.getByCardId("AA11BB22CC33DD")

            // then
            assertThat(result)
                .isRight()
                .transform { it.id }.isEqualTo(userId2)
        }

        @Test
        fun `when getting by unknown card id then returns error`() {
            // given
            val repository = this.repository!! as GettingUserByCardId

            // when
            val unknownCardId = "00000000000000"
            val result = repository.getByCardId(unknownCardId)

            // then
            assertThat(result)
                .isLeft()
                .isEqualTo(
                    Error.UserNotFoundByCardId(
                        "Not able to find user for given card id."
                    )
                )
        }
    }

    @Nested
    internal inner class GivenUsersWithQualificationsStoredInRepository {

        private val userId2 = UserIdFixture.static(978)
        private val userId3 = UserIdFixture.static(999)
        private val qualificationId = QualificationIdFixture.static(456)
        private val qualificationId2 = QualificationIdFixture.static(678)

        private var repository: UserDatabaseRepository? = null

        @BeforeEach
        fun setup() {
            repository = UserDatabaseRepository()

            val user1Created = UserCreated(
                userId,
                actorId,
                firstName = "first1",
                lastName = "last1",
                wikiName = "wiki1"
            )
            repository!!.store(user1Created)

            val user1MemberQualificationAdded = MemberQualificationAdded(
                userId,
                2,
                actorId,
                qualificationId
            )
            repository!!.store(user1MemberQualificationAdded)


            val user1InstructorQualificationAdded = InstructorQualificationAdded(
                userId,
                3,
                actorId,
                qualificationId2
            )
            repository!!.store(user1InstructorQualificationAdded)


            val user2Created = UserCreated(
                userId2,
                actorId,
                firstName = "first2",
                lastName = "last2",
                wikiName = "wiki2"
            )
            repository!!.store(user2Created)

            val user2InstructorQualificationAdded = InstructorQualificationAdded(
                userId2,
                2,
                actorId,
                qualificationId
            )
            repository!!.store(user2InstructorQualificationAdded)

            val user3Created = UserCreated(
                userId3,
                actorId,
                firstName = "first3",
                lastName = "last3",
                wikiName = "wiki3"
            )
            repository!!.store(user3Created)

            val user3MemberQualificationAdded = MemberQualificationAdded(
                userId3,
                2,
                actorId,
                qualificationId
            )
            repository!!.store(user3MemberQualificationAdded)
        }

        @Test
        fun `when getting users by member qualification then returns users who have member qualification`() {
            // given
            val repository = this.repository!! as GettingUsersByMemberQualification

            // when
            val result = repository.getByMemberQualification(qualificationId)

            // then
            assertThat(result)
                .transform { it.map { user -> user.id } }
                .containsExactlyInAnyOrder(userId, userId3)
        }

        @Test
        fun `when getting users by instructor qualification then returns users who have instructor qualification`() {
            // given
            val repository = this.repository!! as GettingUsersByInstructorQualification

            // when
            val result = repository.getByInstructorQualification(qualificationId2)

            // then
            assertThat(result)
                .transform { it.map { user -> user.id } }
                .containsExactlyInAnyOrder(userId)
        }
    }
}