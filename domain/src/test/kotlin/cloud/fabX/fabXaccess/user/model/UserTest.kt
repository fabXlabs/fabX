package cloud.fabX.fabXaccess.user.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.model.AggregateVersionDoesNotIncreaseOneByOne
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.IterableIsEmpty
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFixture
import isLeft
import isNone
import isRight
import isSome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

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
            false,
            null,
            setOf(),
            setOf(),
            null,
            false
        )

        // then
        assertThat(user).isNotNull()
        assertThat(user.id).isEqualTo(userId)
        assertThat(user.aggregateVersion).isEqualTo(aggregateVersion)
    }

    @Test
    fun `when adding new user then returns expected sourcing event`() {
        // given
        DomainModule.configureUserIdFactory { userId }

        val firstName = "first"
        val lastName = "last"
        val wikiName = "wiki"

        val expectedSourcingEvent = UserCreated(
            userId,
            adminActor.id,
            firstName,
            lastName,
            wikiName
        )

        // when
        val result = User.addNew(
            adminActor,
            firstName,
            lastName,
            wikiName
        )

        // then
        assertThat(result).isEqualTo(expectedSourcingEvent)
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
            wikiName = ChangeableValue.ChangeToValue("wiki")
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
    fun `given UserCreated event when constructing user from sourcing events then returns user`() {
        // given
        val userCreated = UserCreated(
            userId,
            adminActor.id,
            firstName = "first",
            lastName = "last",
            wikiName = "wiki"
        )

        // when
        val result = User.fromSourcingEvents(listOf(userCreated))

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(
                User(
                    userId,
                    1,
                    "first",
                    "last",
                    "wiki",
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
    fun `given multiple in-order sourcing events when constructing user then applies all`() {
        // given
        val event1 = UserCreated(
            userId,
            adminActor.id,
            firstName = "first1",
            lastName = "last1",
            wikiName = "wiki1"
        )
        val event2 = UserPersonalInformationChanged(
            userId,
            2,
            adminActor.id,
            firstName = ChangeableValue.ChangeToValue("first2"),
            lastName = ChangeableValue.ChangeToValue("last2"),
            wikiName = ChangeableValue.ChangeToValue("wiki2")
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
            wikiName = ChangeableValue.ChangeToValue("wiki4")
        )
        val event5 = UserPersonalInformationChanged(
            userId,
            5,
            adminActor.id,
            firstName = ChangeableValue.LeaveAsIs,
            lastName = ChangeableValue.ChangeToValue("last5"),
            wikiName = ChangeableValue.ChangeToValue("wiki5")
        )

        // when
        val result = User.fromSourcingEvents(listOf(event1, event2, event3, event4, event5))

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(
                User(
                    userId,
                    5,
                    "first4",
                    "last5",
                    "wiki5",
                    true,
                    "some notes",
                    setOf(),
                    setOf(),
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
            adminActor.id,
            firstName = "first1",
            lastName = "last1",
            wikiName = "wiki1"
        )
        val event3 = UserPersonalInformationChanged(
            userId,
            3,
            adminActor.id,
            firstName = ChangeableValue.ChangeToValue("first2"),
            lastName = ChangeableValue.LeaveAsIs,
            wikiName = ChangeableValue.ChangeToValue("wiki2")
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
    fun `given sourcing events including UserDeleted event when constructing user then returns None`() {
        // given
        val event1 = UserCreated(
            userId,
            adminActor.id,
            firstName = "first1",
            lastName = "last1",
            wikiName = "wiki1"
        )

        val event2 = UserDeleted(
            userId,
            2,
            adminActor.id
        )

        // when
        val result = User.fromSourcingEvents(listOf(event1, event2))

        // then
        assertThat(result)
            .isNone()
    }

    @Test
    fun `when changing personal information then expected sourcing event is returned`() {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = aggregateVersion)

        val expectedSourcingEvent = UserPersonalInformationChanged(
            aggregateRootId = userId,
            aggregateVersion = aggregateVersion + 1,
            actorId = adminActor.id,
            firstName = ChangeableValue.ChangeToValue("newFistName"),
            lastName = ChangeableValue.LeaveAsIs,
            wikiName = ChangeableValue.ChangeToValue("newWikiName")
        )

        // when
        val result = user.changePersonalInformation(
            actor = adminActor,
            firstName = ChangeableValue.ChangeToValue("newFistName"),
            lastName = ChangeableValue.LeaveAsIs,
            wikiName = ChangeableValue.ChangeToValue("newWikiName")
        )

        // then
        assertThat(result).isEqualTo(expectedSourcingEvent)
    }

    @Test
    fun `when changing lock state then expected sourcing event is returned`() {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = aggregateVersion)

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
    fun `when adding username password identity then expected sourcing event is returned`() {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = aggregateVersion)

        val username = "name42"
        val password = "password1234"

        val expectedSourcingEvent = UsernamePasswordIdentityAdded(
            actorId = adminActor.id,
            aggregateRootId = userId,
            aggregateVersion = aggregateVersion + 1,
            username = username,
            password = password
        )

        // when
        val result = user.addUsernamePasswordIdentity(
            adminActor,
            username,
            password
        )

        // then
        assertThat(result).isEqualTo(expectedSourcingEvent)
    }

    @Test
    fun `given username password identity when removing identity then expected sourcing event is returned`() {
        // given
        val username = "u1"
        val identity = UsernamePasswordIdentity(username, "p1")
        val user = UserFixture.withIdentity(identity, userId = userId, aggregateVersion = aggregateVersion)

        val expectedSourcingEvent = UsernamePasswordIdentityRemoved(
            actorId = adminActor.id,
            aggregateRootId = userId,
            aggregateVersion = aggregateVersion + 1,
            username = username
        )

        // when
        val result = user.removeUsernamePasswordIdentity(adminActor, username)

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(expectedSourcingEvent)
    }

    @Test
    fun `given different username password identity when removing identity then error is returned`() {
        // given
        val identity = UsernamePasswordIdentity("u1", "p1")
        val user = UserFixture.withIdentity(identity)

        // when
        val result = user.removeUsernamePasswordIdentity(
            adminActor,
            "unknownusername"
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.UserIdentityNotFound(
                    "Not able to find identity with username \"unknownusername\".",
                    mapOf(
                        "username" to "unknownusername"
                    )
                )
            )
    }

    @Test
    fun `given no username password identity when removing identity then error is returned`() {
        // given
        val user = UserFixture.arbitrary(identities = setOf())

        // when
        val result = user.removeUsernamePasswordIdentity(
            adminActor,
            "unknownusername"
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.UserIdentityNotFound(
                    "Not able to find identity with username \"unknownusername\".",
                    mapOf(
                        "username" to "unknownusername"
                    )
                )
            )
    }

    @Test
    fun `when adding card identity then expected sourcing event is returned`() {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = aggregateVersion)

        val cardId = "02E42232C45D80"
        val cardSecret = "3134b62eebc255fec1b78b542428335e1bf597de8a7be8aff46371b1cc1be91d"

        val expectedSourcingEvent = CardIdentityAdded(
            actorId = adminActor.id,
            aggregateRootId = userId,
            aggregateVersion = aggregateVersion + 1,
            cardId = cardId,
            cardSecret = cardSecret
        )

        // when
        val result = user.addCardIdentity(
            adminActor,
            cardId,
            cardSecret
        )

        // then
        assertThat(result).isEqualTo(expectedSourcingEvent)
    }

    @Test
    fun `given card identity when removing identity then expected sourcing event is returned`() {
        // given
        val cardId = "2646d88c9a77bb"
        val identity = CardIdentity(cardId, "3134b62eebc255fec1b78b542428335e1bf597de8a7be8aff46371b1cc1be91d")
        val user = UserFixture.withIdentity(identity, userId = userId, aggregateVersion = aggregateVersion)

        val expectedSourcingEvent = CardIdentityRemoved(
            actorId = adminActor.id,
            aggregateRootId = userId,
            aggregateVersion = aggregateVersion + 1,
            cardId = cardId
        )

        // when
        val result = user.removeCardIdentity(adminActor, cardId)

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(expectedSourcingEvent)
    }

    @Test
    fun `given different card identity when removing identity then error is returned`() {
        // given
        val cardId = "3c07d626bd4ce5"
        val unknownCardId = "2646d88c9a77bb"

        val identity = CardIdentity(cardId, "dcc3114e9f37ec873fc4f043db6209ac948fe27b184ba8df3a2549")
        val user = UserFixture.withIdentity(identity, userId = userId, aggregateVersion = aggregateVersion)

        // when
        val result = user.removeCardIdentity(adminActor, unknownCardId)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.UserIdentityNotFound(
                    "Not able to find identity with card id $unknownCardId.",
                    mapOf(
                        "cardId" to unknownCardId
                    )
                )
            )
    }

    @Test
    fun `given no card identity when removing identity then error is returned`() {
        // given
        val unknownCardId = "2646d88c9a77bb"

        val user = UserFixture.arbitrary(userId, identities = setOf())

        // when
        val result = user.removeCardIdentity(adminActor, unknownCardId)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.UserIdentityNotFound(
                    "Not able to find identity with card id $unknownCardId.",
                    mapOf(
                        "cardId" to unknownCardId
                    )
                )
            )
    }

    @Test
    fun `when adding phone number identity then expected sourcing event is returned`() {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = aggregateVersion)

        val phoneNr = "+491720000000"

        val expectedSourcingEvent = PhoneNrIdentityAdded(
            actorId = adminActor.id,
            aggregateRootId = userId,
            aggregateVersion = aggregateVersion + 1,
            phoneNr = phoneNr
        )

        // when
        val result = user.addPhoneNrIdentity(
            adminActor,
            phoneNr
        )

        // then
        assertThat(result).isEqualTo(expectedSourcingEvent)
    }

    @Test
    fun `given phone number identity when removing identity then expected sourcing event is returned`() {
        // given
        val phoneNr = "+491230000000"
        val identity = PhoneNrIdentity(phoneNr)
        val user = UserFixture.withIdentity(identity, userId = userId, aggregateVersion = aggregateVersion)

        val expectedSourcingEvent = PhoneNrIdentityRemoved(
            actorId = adminActor.id,
            aggregateRootId = userId,
            aggregateVersion = aggregateVersion + 1,
            phoneNr = phoneNr
        )

        // when
        val result = user.removePhoneNrIdentity(adminActor, phoneNr)

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(expectedSourcingEvent)
    }

    @Test
    fun `given different phone number identity when removing identity then error is returned`() {
        // given
        val identity = PhoneNrIdentity("+424242")
        val user = UserFixture.withIdentity(identity)

        // when
        val result = user.removePhoneNrIdentity(
            adminActor,
            "+123"
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.UserIdentityNotFound(
                    "Not able to find identity with phone number +123.",
                    mapOf("phoneNr" to "+123")
                )
            )
    }

    @Test
    fun `given no phone number identity when removing identity then error is returned`() {
        // given
        val user = UserFixture.arbitrary(identities = setOf())

        // when
        val result = user.removePhoneNrIdentity(
            adminActor,
            "+4242123"
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.UserIdentityNotFound(
                    "Not able to find identity with phone number +4242123.",
                    mapOf("phoneNr" to "+4242123")
                )
            )
    }

    @Test
    fun `when deleting then expected sourcing event is returned`() {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = aggregateVersion)

        val expectedSourcingEvent = UserDeleted(
            aggregateRootId = userId,
            aggregateVersion = aggregateVersion + 1,
            actorId = adminActor.id
        )

        // when
        val result = user.delete(adminActor)

        // then
        assertThat(result).isEqualTo(expectedSourcingEvent)
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "user123, password42, true",
            "user123, password,   false",
            "user,    password42, false",
            "user,    password,   false",
        ]
    )
    fun `given user with username password identity when checking hasIdentity then returns expected result`(
        username: String,
        password: String,
        expectedResult: Boolean
    ) {
        // given
        val identity = UsernamePasswordIdentity("user123", "password42")
        val user = UserFixture.withIdentity(identity)

        // when
        val result = user.hasIdentity(UsernamePasswordIdentity(username, password))

        // then
        assertThat(result).isEqualTo(expectedResult)
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "aabbccddeeff, 42aa42aa42aa, true",
            "aaaaaaaaaaaa, 42aa42aa42aa, false",
            "aabbccddeeff, bbbbbbbbbbbb, false",
            "aaaaaaaaaaaa, bbbbbbbbbbbb, false",
        ]
    )
    fun `given user with card identity when checking hasIdentity then returns expected result`(
        cardId: String,
        cardSecret: String,
        expectedResult: Boolean
    ) {
        // given
        val identity = CardIdentity("aabbccddeeff", "42aa42aa42aa")
        val user = UserFixture.withIdentity(identity)

        // when
        val result = user.hasIdentity(CardIdentity(cardId, cardSecret))

        // then
        assertThat(result).isEqualTo(expectedResult)
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "+4912300000042, true",
            "+4912300000001, false",
        ]
    )
    fun `given user with phone number identity when checking hasIdentity then returns expected result`(
        phoneNr: String,
        expectedResult: Boolean
    ) {
        // given
        val identity = PhoneNrIdentity("+4912300000042")
        val user = UserFixture.withIdentity(identity)

        // when
        val result = user.hasIdentity(PhoneNrIdentity(phoneNr))

        // then
        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `given any user when getting as member then returns member`() {
        // given
        val qualifications = setOf(QualificationIdFixture.arbitraryId(), QualificationIdFixture.arbitraryId())
        val user = UserFixture.arbitrary(
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
        val user = UserFixture.arbitrary(userId, instructorQualifications = null)

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

        val user = UserFixture.arbitrary(
            userId,
            instructorQualifications = setOf(qualification1, qualification2),
            firstName = "first",
            lastName = "last"
        )

        // when
        val result = user.asInstructor()

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(Instructor(userId, "first last", setOf(qualification1, qualification2)))
    }

    @Test
    fun `given user without admin when getting as admin then returns error`() {
        // given
        val user = UserFixture.arbitrary(userId, isAdmin = false)

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
        val user = UserFixture.arbitrary(
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
            false,
            null,
            setOf(),
            setOf(QualificationIdFixture.staticId(43)),
            setOf(QualificationIdFixture.staticId(44)),
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
                    "locked=false, " +
                    "notes=null, " +
                    "identities=[], " +
                    "memberQualifications=[QualificationId(value=6ecbc07c-382b-3e04-a9b3-a86909f10e64)], " +
                    "instructorQualifications=[QualificationId(value=a3dd6fd6-61a5-3c37-810c-8c68fe610bec)], " +
                    "isAdmin=true)"
        )
    }
}