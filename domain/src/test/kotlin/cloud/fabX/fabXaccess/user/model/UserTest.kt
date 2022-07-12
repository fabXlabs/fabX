package cloud.fabX.fabXaccess.user.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import cloud.fabX.fabXaccess.common.model.AggregateVersionDoesNotIncreaseOneByOne
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.IterableIsEmpty
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFixture
import isLeft
import isRight
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class UserTest {

    private val userId = UserIdFixture.arbitraryId()
    private val aggregateVersion = 42L

    private val adminActor = AdminFixture.arbitraryAdmin()

    @Test
    fun `given valid values when constructing user then user is constructed`() {
        // given

        // when
        val user = User(
            userId,
            aggregateVersion,
            "Nikola",
            "Tesla",
            "nick",
            "00491234567890",
            false,
            null,
            listOf(),
            null,
            false
        )

        // then
        assertThat(user).isNotNull()
        assertThat(user.id).isEqualTo(userId)
        assertThat(user.aggregateVersion).isEqualTo(aggregateVersion)
    }

    @Test
    fun `given no sourcing events when constructing user from sourcing events then throws exception`() {
        // given

        // when
        val exception = assertThrows<IterableIsEmpty> {
            User.fromSourcingEvents(listOf())
        }

        // then
        assertThat(exception.message)
            .isEqualTo("No sourcing events contained in iterable.")
    }

    @Test
    fun `given no UserCreated event when constructing user from sourcing events then throws exception`() {
        // given
        val event = UserPersonalInformationChanged(
            userId,
            1,
            adminActor.id,
            firstName = ChangeableValue.ChangeToValue("first"),
            lastName = ChangeableValue.ChangeToValue("last"),
            wikiName = ChangeableValue.ChangeToValue("wiki"),
            phoneNumber = ChangeableValue.ChangeToValue(null)
        )

        // when
        val exception = assertThrows<User.EventHistoryDoesNotStartWithUserCreated> {
            User.fromSourcingEvents(listOf(event))
        }

        // then
        assertThat(exception.message)
            .isNotNull()
            .isEqualTo("Event history starts with $event, not a UserCreated event.")
    }

    @Test
    fun `given out-of-order UserCreated event when constructing user from sourcing events then throws exception`() {
        // given
        val event1 = UserPersonalInformationChanged(
            userId,
            1,
            adminActor.id,
            firstName = ChangeableValue.ChangeToValue("first"),
            lastName = ChangeableValue.ChangeToValue("last"),
            wikiName = ChangeableValue.ChangeToValue("wiki"),
            phoneNumber = ChangeableValue.ChangeToValue("1")
        )

        val event2 = UserCreated(
            userId,
            2,
            adminActor.id,
            firstName = "first",
            lastName = "last",
            wikiName = "wiki",
            phoneNumber = null
        )

        // when
        val exception = assertThrows<User.EventHistoryDoesNotStartWithUserCreated> {
            User.fromSourcingEvents(listOf(event1, event2))
        }

        // then
        assertThat(exception.message)
            .isNotNull()
            .isEqualTo("Event history starts with $event1, not a UserCreated event.")
    }

    @Test
    fun `given UserCreated event when constructing user from sourcing events then returns user`() {
        // given
        val userCreated = UserCreated(
            userId,
            1,
            adminActor.id,
            firstName = "first",
            lastName = "last",
            wikiName = "wiki",
            phoneNumber = null
        )

        // when
        val result = User.fromSourcingEvents(listOf(userCreated))

        // then
        assertThat(result).isEqualTo(
            User(
                userId,
                1,
                "first",
                "last",
                "wiki",
                null,
                false,
                null,
                listOf(),
                null,
                false
            )
        )
    }

    @Test
    fun `given multiple in-order sourcing events when constructing user then applies all`() {
        // given
        val event1 = UserCreated(
            userId,
            1,
            adminActor.id,
            firstName = "first1",
            lastName = "last1",
            wikiName = "wiki1",
            phoneNumber = "1"
        )
        val event2 = UserPersonalInformationChanged(
            userId,
            2,
            adminActor.id,
            firstName = ChangeableValue.ChangeToValue("first2"),
            lastName = ChangeableValue.ChangeToValue("last2"),
            wikiName = ChangeableValue.ChangeToValue("wiki2"),
            phoneNumber = ChangeableValue.ChangeToValue("2")
        )
        val event3 = UserLockStateChanged(
            userId,
            3,
            adminActor.id,
            locked = ChangeableValue.ChangeToValue(true),
            notes = ChangeableValue.ChangeToValue("some notes")
        )
        val event4 = UserPersonalInformationChanged(
            userId,
            4,
            adminActor.id,
            firstName = ChangeableValue.ChangeToValue("first4"),
            lastName = ChangeableValue.LeaveAsIs,
            wikiName = ChangeableValue.ChangeToValue("wiki4"),
            phoneNumber = ChangeableValue.LeaveAsIs
        )
        val event5 = UserPersonalInformationChanged(
            userId,
            5,
            adminActor.id,
            firstName = ChangeableValue.LeaveAsIs,
            lastName = ChangeableValue.ChangeToValue("last5"),
            wikiName = ChangeableValue.ChangeToValue("wiki5"),
            phoneNumber = ChangeableValue.ChangeToValue(null)
        )

        // when
        val result = User.fromSourcingEvents(listOf(event1, event2, event3, event4, event5))

        // then
        assertThat(result).isEqualTo(
            User(
                userId,
                5,
                "first4",
                "last5",
                "wiki5",
                null,
                true,
                "some notes",
                listOf(),
                null,
                false
            )
        )
    }

    @Test
    fun `given multiple out-of-order sourcing events when constructing user then throws exception`() {
        // given
        val event1 = UserCreated(
            userId,
            1,
            adminActor.id,
            firstName = "first1",
            lastName = "last1",
            wikiName = "wiki1",
            phoneNumber = "1"
        )
        val event3 = UserPersonalInformationChanged(
            userId,
            3,
            adminActor.id,
            firstName = ChangeableValue.ChangeToValue("first2"),
            lastName = ChangeableValue.LeaveAsIs,
            wikiName = ChangeableValue.ChangeToValue("wiki2"),
            phoneNumber = ChangeableValue.LeaveAsIs
        )
        val event2 = UserLockStateChanged(
            userId,
            2,
            adminActor.id,
            locked = ChangeableValue.ChangeToValue(true),
            notes = ChangeableValue.ChangeToValue("some notes")
        )

        // then
        val exception = assertThrows<AggregateVersionDoesNotIncreaseOneByOne> {
            User.fromSourcingEvents(listOf(event1, event3, event2))
        }

        // then
        assertThat(exception.message)
            .isNotNull()
            .isEqualTo("Aggregate version does not increase one by one for ${listOf(event1, event3, event2)}.")
    }

    @Test
    fun `when changing personal information then expected sourcing event is returned`() {
        // given
        val user = UserFixture.arbitraryUser(userId, aggregateVersion = aggregateVersion)

        val expectedSourcingEvent = UserPersonalInformationChanged(
            aggregateRootId = userId,
            aggregateVersion = aggregateVersion + 1,
            actorId = adminActor.id,
            firstName = ChangeableValue.ChangeToValue("newFistName"),
            lastName = ChangeableValue.LeaveAsIs,
            wikiName = ChangeableValue.ChangeToValue("newWikiName"),
            phoneNumber = ChangeableValue.ChangeToValue(null)
        )

        // when
        val result = user.changePersonalInformation(
            actor = adminActor,
            firstName = ChangeableValue.ChangeToValue("newFistName"),
            lastName = ChangeableValue.LeaveAsIs,
            wikiName = ChangeableValue.ChangeToValue("newWikiName"),
            phoneNumber = ChangeableValue.ChangeToValue(null)
        )

        // then
        assertThat(result).isEqualTo(expectedSourcingEvent)
    }

    @Test
    fun `when changing lock state then expected sourcing event is returned`() {
        // given
        val user = UserFixture.arbitraryUser(userId, aggregateVersion = aggregateVersion)

        val expectedSourcingEvent = UserLockStateChanged(
            actorId = adminActor.id,
            aggregateRootId = userId,
            aggregateVersion = aggregateVersion + 1,
            locked = ChangeableValue.ChangeToValue(true),
            notes = ChangeableValue.ChangeToValue(null)
        )

        // when
        val result = user.changeLockState(
            actor = adminActor,
            locked = ChangeableValue.ChangeToValue(true),
            notes = ChangeableValue.ChangeToValue(null)
        )

        // then
        assertThat(result).isEqualTo(expectedSourcingEvent)
    }

    @Test
    fun `given any user when getting as member then returns member`() {
        // given
        val qualifications = listOf(QualificationIdFixture.arbitraryId(), QualificationIdFixture.arbitraryId())
        val user = UserFixture.arbitraryUser(
            userId,
            memberQualifications = qualifications,
            firstName = "firstName",
            lastName = "lastName"
        )

        // when
        val result = user.asMember()

        // then
        assertThat(result).isEqualTo(Member(userId, "firstName lastName", qualifications))
    }

    @Test
    fun `given user without instructor when getting as instructor then returns error`() {
        // given
        val user = UserFixture.arbitraryUser(userId, instructorQualifications = null)

        // when
        val result = user.asInstructor()

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(Error.UserNotInstructor("User $userId is not an instructor."))
    }

    @Test
    fun `given user with instructor when getting as instructor then returns instructor`() {
        // given
        val qualification1 = QualificationIdFixture.arbitraryId()
        val qualification2 = QualificationIdFixture.arbitraryId()

        val user = UserFixture.arbitraryUser(
            userId,
            instructorQualifications = listOf(qualification1, qualification2),
            firstName = "first",
            lastName = "last"
        )

        // when
        val result = user.asInstructor()

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(Instructor(userId, "first last", listOf(qualification1, qualification2)))
    }

    @Test
    fun `given user without admin when getting as admin then returns error`() {
        // given
        val user = UserFixture.arbitraryUser(userId, isAdmin = false)

        // when
        val result = user.asAdmin()

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(Error.UserNotAdmin("User $userId is not an admin."))
    }

    @Test
    fun `given user with admin when getting as admin then returns admin`() {
        // given
        val user = UserFixture.arbitraryUser(
            userId,
            firstName = "first",
            lastName = "last",
            isAdmin = true
        )

        // when
        val result = user.asAdmin()

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(Admin(userId, "first last"))
    }

    @Test
    fun `given valid user when stringifying then result is correct`() {
        // given
        val user = User(
            UserIdFixture.staticId(42),
            123,
            "Nikola",
            "Tesla",
            "nick",
            "00491234567890",
            false,
            null,
            listOf(QualificationIdFixture.staticId(43)),
            listOf(QualificationIdFixture.staticId(44)),
            true
        )

        // when
        val result = user.toString()

        // then
        assertThat(result).isEqualTo(
            "User(id=UserId(value=f4a3f34c-e12a-395c-9fd2-23e167422c32), " +
                    "aggregateVersion=123, " +
                    "firstName=Nikola, " +
                    "lastName=Tesla, " +
                    "wikiName=nick, " +
                    "phoneNumber=00491234567890, " +
                    "locked=false, " +
                    "notes=null, " +
                    "memberQualifications=[QualificationId(value=6ecbc07c-382b-3e04-a9b3-a86909f10e64)], " +
                    "instructorQualifications=[QualificationId(value=a3dd6fd6-61a5-3c37-810c-8c68fe610bec)], " +
                    "isAdmin=true)"
        )
    }
}