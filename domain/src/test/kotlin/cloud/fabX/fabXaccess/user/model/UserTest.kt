package cloud.fabX.fabXaccess.user.model

import FixedClock
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import cloud.fabX.fabXaccess.common.model.AggregateVersionDoesNotIncreaseOneByOne
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.ErrorFixture
import cloud.fabX.fabXaccess.common.model.IterableIsEmpty
import cloud.fabX.fabXaccess.common.model.QualificationDeleted
import cloud.fabX.fabXaccess.common.model.newUserId
import cloud.fabX.fabXaccess.qualification.model.QualificationFixture
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFixture
import isLeft
import isNone
import isRight
import isSome
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource

@OptIn(ExperimentalCoroutinesApi::class)
internal class UserTest {

    private val adminActor = AdminFixture.arbitrary()
    private val correlationId = CorrelationIdFixture.arbitrary()

    private val fixedInstant = Clock.System.now()
    private val fixedClock = FixedClock(fixedInstant)

    private val userId = UserIdFixture.arbitrary()
    private val aggregateVersion = 42L

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
    fun `when adding new user then returns expected sourcing event`() = runTest {
        // given
        val firstName = "first"
        val lastName = "last"
        val wikiName = "wiki"

        val expectedSourcingEvent = UserCreated(
            userId,
            adminActor.id,
            fixedInstant,
            correlationId,
            firstName,
            lastName,
            wikiName
        )

        // when
        val result = User.addNew(
            { userId },
            adminActor,
            fixedClock,
            correlationId,
            firstName,
            lastName,
            wikiName
        ) {
            if (it == wikiName) {
                Error.UserNotFoundByWikiName("").left()
            } else {
                throw IllegalArgumentException("unexpected wiki name")
            }
        }

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(expectedSourcingEvent)
    }

    @Test
    fun `given wiki name is already in use when adding new user then returns error`() = runTest {
        // given
        val otherUser = UserFixture.arbitrary(wikiName = "someone")

        // when
        val result = User.addNew(
            { newUserId() },
            adminActor,
            fixedClock,
            correlationId,
            "first",
            "last",
            "someone"
        ) {
            if (it == "someone") {
                otherUser.right()
            } else {
                throw IllegalArgumentException("unexpected wiki name")
            }
        }

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.WikiNameAlreadyInUse("Wiki name is already in use.", correlationId)
            )
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
            fixedInstant,
            CorrelationIdFixture.arbitrary(),
            firstName = ChangeableValue.ChangeToValueString("first"),
            lastName = ChangeableValue.ChangeToValueString("last"),
            wikiName = ChangeableValue.ChangeToValueString("wiki")
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
            fixedInstant,
            CorrelationIdFixture.arbitrary(),
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
            fixedInstant,
            CorrelationIdFixture.arbitrary(),
            firstName = "first1",
            lastName = "last1",
            wikiName = "wiki1"
        )
        val event2 = UserPersonalInformationChanged(
            userId,
            2,
            adminActor.id,
            fixedInstant,
            CorrelationIdFixture.arbitrary(),
            firstName = ChangeableValue.ChangeToValueString("first2"),
            lastName = ChangeableValue.ChangeToValueString("last2"),
            wikiName = ChangeableValue.ChangeToValueString("wiki2")
        )
        val event3 = UserLockStateChanged(
            userId,
            3,
            adminActor.id,
            fixedInstant,
            CorrelationIdFixture.arbitrary(),
            locked = ChangeableValue.ChangeToValueBoolean(true),
            notes = ChangeableValue.ChangeToValueString("some notes")
        )
        val event4 = UserPersonalInformationChanged(
            userId,
            4,
            adminActor.id,
            fixedInstant,
            CorrelationIdFixture.arbitrary(),
            firstName = ChangeableValue.ChangeToValueString("first4"),
            lastName = ChangeableValue.LeaveAsIs,
            wikiName = ChangeableValue.ChangeToValueString("wiki4")
        )
        val event5 = UserPersonalInformationChanged(
            userId,
            5,
            adminActor.id,
            fixedInstant,
            CorrelationIdFixture.arbitrary(),
            firstName = ChangeableValue.LeaveAsIs,
            lastName = ChangeableValue.ChangeToValueString("last5"),
            wikiName = ChangeableValue.ChangeToValueString("wiki5")
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
            fixedInstant,
            CorrelationIdFixture.arbitrary(),
            firstName = "first1",
            lastName = "last1",
            wikiName = "wiki1"
        )
        val event3 = UserPersonalInformationChanged(
            userId,
            3,
            adminActor.id,
            fixedInstant,
            CorrelationIdFixture.arbitrary(),
            firstName = ChangeableValue.ChangeToValueString("first2"),
            lastName = ChangeableValue.LeaveAsIs,
            wikiName = ChangeableValue.ChangeToValueString("wiki2")
        )
        val event2 = UserLockStateChanged(
            userId,
            2,
            adminActor.id,
            fixedInstant,
            CorrelationIdFixture.arbitrary(),
            locked = ChangeableValue.ChangeToValueBoolean(true),
            notes = ChangeableValue.ChangeToValueString("some notes")
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
    fun `given sourcing events including MemberQualificationAdded then applies it`() {
        // given
        val qualificationId1 = QualificationIdFixture.arbitrary()
        val qualificationId2 = QualificationIdFixture.arbitrary()

        val user = UserFixture.arbitrary(
            userId = userId,
            aggregateVersion = 1,
            memberQualifications = setOf()
        )

        val event1 = MemberQualificationAdded(
            userId,
            2,
            adminActor.id,
            fixedInstant,
            CorrelationIdFixture.arbitrary(),
            qualificationId1
        )

        val event2 = MemberQualificationAdded(
            userId,
            3,
            adminActor.id,
            fixedInstant,
            CorrelationIdFixture.arbitrary(),
            qualificationId2
        )

        // when
        val result = user.apply(event1).flatMap { it.apply(event2) }

        // then
        assertThat(result)
            .isSome()
            .transform { it.memberQualifications }
            .containsExactlyInAnyOrder(qualificationId1, qualificationId2)
    }

    @ParameterizedTest
    @ValueSource(ints = [1, 2, 42])
    fun `given sourcing events including MemberQualificationRemoved then applies it`(
        amountAdditionalQualifications: Int
    ) {
        // given
        val qualificationId1 = QualificationIdFixture.arbitrary()

        val additionalQualificationIds = QualificationIdFixture.arbitraries(amountAdditionalQualifications)

        val user = UserFixture.arbitrary(
            userId = userId,
            aggregateVersion = 42,
            memberQualifications = setOf(qualificationId1) + additionalQualificationIds
        )

        val event = MemberQualificationRemoved(
            userId,
            43,
            adminActor.id,
            fixedInstant,
            CorrelationIdFixture.arbitrary(),
            qualificationId1
        )

        // when
        val result = user.apply(event)

        // then
        assertThat(result)
            .isSome()
            .transform { it.memberQualifications }
            .isEqualTo(additionalQualificationIds)
    }

    @Test
    fun `given sourcing events including InstructorQualificationAdded then applies it`() {
        // given
        val qualificationId1 = QualificationIdFixture.arbitrary()
        val qualificationId2 = QualificationIdFixture.arbitrary()

        val user = UserFixture.arbitrary(
            userId = userId,
            aggregateVersion = 1,
            instructorQualifications = null
        )

        val event1 = InstructorQualificationAdded(
            userId,
            2,
            adminActor.id,
            fixedInstant,
            CorrelationIdFixture.arbitrary(),
            qualificationId1
        )

        val event2 = InstructorQualificationAdded(
            userId,
            3,
            adminActor.id,
            fixedInstant,
            CorrelationIdFixture.arbitrary(),
            qualificationId2
        )

        // when
        val result = user.apply(event1).flatMap { it.apply(event2) }

        // then
        assertThat(result)
            .isSome()
            .transform { it.instructorQualifications }
            .isNotNull()
            .containsExactlyInAnyOrder(qualificationId1, qualificationId2)
    }

    @ParameterizedTest
    @ValueSource(ints = [1, 2, 42])
    fun `given sourcing events including InstructorQualificationRemoved then applies it`(
        amountAdditionalQualifications: Int
    ) {
        // given
        val qualificationId = QualificationIdFixture.arbitrary()

        val additionalQualificationIds = QualificationIdFixture.arbitraries(amountAdditionalQualifications)

        val user = UserFixture.arbitrary(
            userId = userId,
            aggregateVersion = 42,
            instructorQualifications = setOf(qualificationId) + additionalQualificationIds
        )

        val event = InstructorQualificationRemoved(
            userId,
            43,
            adminActor.id,
            fixedInstant,
            CorrelationIdFixture.arbitrary(),
            qualificationId
        )

        // when
        val result = user.apply(event)

        // then
        assertThat(result)
            .isSome()
            .transform { it.instructorQualifications }
            .isNotNull()
            .isEqualTo(additionalQualificationIds)
    }

    @Test
    fun `given sourcing events including InstructorQualificationRemoved removing last instructor qualification then applies it`() {
        // given
        val qualificationId1 = QualificationIdFixture.arbitrary()
        val qualificationId2 = QualificationIdFixture.arbitrary()

        val user = UserFixture.arbitrary(
            userId = userId,
            aggregateVersion = 42,
            instructorQualifications = setOf(qualificationId1, qualificationId2)
        )

        val event1 = InstructorQualificationRemoved(
            userId,
            43,
            adminActor.id,
            fixedInstant,
            CorrelationIdFixture.arbitrary(),
            qualificationId1
        )

        val event2 = InstructorQualificationRemoved(
            userId,
            43,
            adminActor.id,
            fixedInstant,
            CorrelationIdFixture.arbitrary(),
            qualificationId2
        )

        // when
        val result = user.apply(event1).flatMap { it.apply(event2) }

        // then
        assertThat(result)
            .isSome()
            .transform { it.instructorQualifications }
            .isNull()
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "false, true,  true",
            "false, false, false",
            "true,  true,  true",
            "true,  false, false",
        ]
    )
    fun `given sourcing events including IsAdminChanged event when constructing user then returns applies it`(
        initial: Boolean, changingTo: Boolean, expectedResult: Boolean
    ) {
        // given
        val user = UserFixture.arbitrary(
            userId = userId,
            aggregateVersion = 42,
            isAdmin = initial
        )

        val event = IsAdminChanged(
            userId,
            43,
            adminActor.id,
            fixedInstant,
            CorrelationIdFixture.arbitrary(),
            changingTo
        )

        // when
        val result = user.apply(event)

        // then
        assertThat(result)
            .isSome()
            .transform { it.isAdmin }
            .isEqualTo(expectedResult)
    }

    @Test
    fun `given sourcing events including UserDeleted event when constructing user then returns None`() {
        // given
        val event1 = UserCreated(
            userId,
            adminActor.id,
            fixedInstant,
            CorrelationIdFixture.arbitrary(),
            firstName = "first1",
            lastName = "last1",
            wikiName = "wiki1"
        )

        val event2 = UserDeleted(
            userId,
            2,
            adminActor.id,
            fixedInstant,
            CorrelationIdFixture.arbitrary(),
        )

        // when
        val result = User.fromSourcingEvents(listOf(event1, event2))

        // then
        assertThat(result)
            .isNone()
    }

    @Test
    fun `when changing personal information then expected sourcing event is returned`() = runTest {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = aggregateVersion)

        val expectedSourcingEvent = UserPersonalInformationChanged(
            userId,
            aggregateVersion + 1,
            adminActor.id,
            fixedInstant,
            correlationId,
            firstName = ChangeableValue.ChangeToValueString("newFistName"),
            lastName = ChangeableValue.LeaveAsIs,
            wikiName = ChangeableValue.ChangeToValueString("newWikiName")
        )

        // when
        val result = user.changePersonalInformation(
            adminActor,
            fixedClock,
            correlationId,
            firstName = ChangeableValue.ChangeToValueString("newFistName"),
            lastName = ChangeableValue.LeaveAsIs,
            wikiName = ChangeableValue.ChangeToValueString("newWikiName")
        ) {
            if (it == "newWikiName") {
                Error.UserNotFoundByWikiName("").left()
            } else {
                throw IllegalArgumentException("unexpected wiki name")
            }
        }

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(expectedSourcingEvent)
    }

    @Test
    fun `given wiki name is already in use when changing personal information then returns error`() = runTest {
        // given
        val otherUser = UserFixture.arbitrary(wikiName = "wikiName")

        val user = UserFixture.arbitrary(userId, aggregateVersion = aggregateVersion)

        // when
        val result = user.changePersonalInformation(
            adminActor,
            fixedClock,
            correlationId,
            wikiName = ChangeableValue.ChangeToValueString("wikiName")
        ) {
            if (it == "wikiName") {
                otherUser.right()
            } else {
                throw IllegalArgumentException("unexpected wiki name")
            }
        }

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(Error.WikiNameAlreadyInUse("Wiki name is already in use.", correlationId))
    }

    @Test
    fun `when changing lock state then expected sourcing event is returned`() {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = aggregateVersion)

        val expectedSourcingEvent = UserLockStateChanged(
            userId,
            aggregateVersion + 1,
            adminActor.id,
            fixedInstant,
            correlationId,
            locked = ChangeableValue.ChangeToValueBoolean(true),
            notes = ChangeableValue.ChangeToValueOptionalString(null)
        )

        // when
        val result = user.changeLockState(
            adminActor,
            fixedClock,
            correlationId,
            locked = ChangeableValue.ChangeToValueBoolean(true),
            notes = ChangeableValue.ChangeToValueOptionalString(null)
        )

        // then
        assertThat(result).isEqualTo(expectedSourcingEvent)
    }

    @Test
    fun `when adding username password identity then expected sourcing event is returned`() = runTest {
        // given
        val user = UserFixture.arbitrary(
            userId,
            aggregateVersion = aggregateVersion,
            identities = setOf(UserIdentityFixture.card())
        )

        val username = "name42"
        val hash = "Q1kxQjCP56+cO62gMvOP+T9gz0uEnZIOnzT7TeJ21V4="

        val expectedSourcingEvent = UsernamePasswordIdentityAdded(
            userId,
            aggregateVersion + 1,
            adminActor.id,
            fixedInstant,
            correlationId,
            username = username,
            hash = hash
        )

        // when
        val result = user.addUsernamePasswordIdentity(
            adminActor,
            fixedClock,
            correlationId,
            username,
            hash
        ) {
            if (it == username) {
                Error.UserNotFoundByUsername("").left()
            } else {
                throw IllegalArgumentException("unexpected username")
            }
        }

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(expectedSourcingEvent)
    }

    @Test
    fun `given user already has username password identity when adding username password identity then returns error`() =
        runTest {
            // given
            val identity = UserIdentityFixture.usernamePassword("username")
            val user = UserFixture.arbitrary(userId, aggregateVersion = aggregateVersion, identities = setOf(identity))

            // when
            val result = user.addUsernamePasswordIdentity(
                adminActor,
                fixedClock,
                correlationId,
                "name",
                "hash"
            ) {
                if (it == "name") {
                    Error.UserNotFoundByUsername("").left()
                } else {
                    throw IllegalArgumentException("unexpected username")
                }
            }

            // then
            assertThat(result)
                .isLeft()
                .isEqualTo(
                    Error.UsernamePasswordIdentityAlreadyFound(
                        "User already has a username password identity.",
                        correlationId
                    )
                )
        }

    @Test
    fun `given username is already in use when adding username password identity then returns error`() = runTest {
        // given
        val otherUser = UserFixture.arbitrary(
            identities = setOf(UserIdentityFixture.usernamePassword("name"))
        )

        val user = UserFixture.arbitrary(userId, aggregateVersion = aggregateVersion, identities = setOf())

        // when
        val result = user.addUsernamePasswordIdentity(
            adminActor,
            fixedClock,
            correlationId,
            "name",
            "hash"
        ) {
            if (it == "name") {
                otherUser.right()
            } else {
                throw IllegalArgumentException("unexpected username")
            }
        }

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.UsernameAlreadyInUse(
                    "Username is already in use.",
                    correlationId
                )
            )
    }

    @Test
    fun `when changing password then expected sourcing event is returned`() = runTest {
        // given
        val identity = UserIdentityFixture.usernamePassword()
        val user = UserFixture.withIdentity(identity, userId = userId, aggregateVersion = aggregateVersion)

        val hash = "G3T2Uf9olbUkPV2lFXfgqi61iyCC7i1c2qRTtV1vhNQ="

        val expectedSourcingEvent = PasswordChanged(
            userId,
            aggregateVersion + 1,
            userId,
            fixedInstant,
            correlationId,
            hash = hash
        )

        // when
        val result = user.changePassword(user.asMember(), fixedClock, correlationId, hash)

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(expectedSourcingEvent)
    }

    @Test
    fun `given other actor when changing password then error is returned`() = runTest {
        // given
        val identity = UserIdentityFixture.usernamePassword()
        val user = UserFixture.withIdentity(identity)

        val actor = UserFixture.arbitrary()

        // when
        val result = user.changePassword(
            actor.asMember(),
            fixedClock,
            correlationId,
            "G3T2Uf9olbUkPV2lFXfgqi61iyCC7i1c2qRTtV1vhNQ="
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.UserNotActor(
                    "User is not actor.",
                    correlationId
                )
            )
    }

    @Test
    fun `given no username password identity when changing password then error is returned`() = runTest {
        // given
        val user = UserFixture.arbitrary(identities = setOf())

        // when
        val result = user.changePassword(
            user.asMember(),
            fixedClock,
            correlationId,
            "G3T2Uf9olbUkPV2lFXfgqi61iyCC7i1c2qRTtV1vhNQ="
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.UsernamePasswordIdentityNotFound(
                    "Not able to find username password identity.",
                    correlationId
                )
            )
    }

    @Test
    fun `given invalid hash when changing password then error is returned`() = runTest {
        // given
        val identity = UserIdentityFixture.usernamePassword()
        val user = UserFixture.withIdentity(identity, userId = userId, aggregateVersion = aggregateVersion)

        val invalidHash = "invalidHash"

        // when
        val result = user.changePassword(
            user.asMember(),
            fixedClock,
            correlationId,
            invalidHash
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.PasswordHashInvalid(
                    "Password hash is invalid (has to match ${UsernamePasswordIdentity.hashRegex}).",
                    invalidHash,
                    UsernamePasswordIdentity.hashRegex,
                    correlationId
                )
            )
    }

    @Test
    fun `given username password identity when removing identity then expected sourcing event is returned`() = runTest {
        // given
        val username = "u1"
        val identity = UserIdentityFixture.usernamePassword(username)
        val user = UserFixture.withIdentity(identity, userId = userId, aggregateVersion = aggregateVersion)

        val expectedSourcingEvent = UsernamePasswordIdentityRemoved(
            userId,
            aggregateVersion + 1,
            adminActor.id,
            fixedInstant,
            correlationId,
            username = username
        )

        // when
        val result = user.removeUsernamePasswordIdentity(adminActor, fixedClock, correlationId, username)

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(expectedSourcingEvent)
    }

    @Test
    fun `given different username password identity when removing identity then error is returned`() = runTest {
        // given
        val identity = UserIdentityFixture.usernamePassword("u1")
        val user = UserFixture.withIdentity(identity)

        // when
        val result = user.removeUsernamePasswordIdentity(
            adminActor,
            fixedClock,
            correlationId,
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
                    ),
                    correlationId
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
            fixedClock,
            correlationId,
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
                    ),
                    correlationId
                )
            )
    }

    @Test
    fun `when adding card identity then expected sourcing event is returned`() = runTest {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = aggregateVersion)

        val cardId = "02E42232C45D80"
        val cardSecret = "D7673E42DA311BFF83EBD0761ED8CD607200A9E0F6065E6B945AE86D14EAB3C6"

        val expectedSourcingEvent = CardIdentityAdded(
            userId,
            aggregateVersion + 1,
            adminActor.id,
            fixedInstant,
            correlationId,
            cardId = cardId,
            cardSecret = cardSecret
        )

        // when
        val result = user.addCardIdentity(
            adminActor,
            fixedClock,
            correlationId,
            cardId,
            cardSecret
        ) {
            if (it == cardId) {
                Error.UserNotFoundByCardId("").left()
            } else {
                throw IllegalArgumentException("unexpected card id")
            }
        }

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(expectedSourcingEvent)
    }

    @Test
    fun `given card id is in use when adding card identity then returns error`() = runTest {
        // given
        val cardId = "02E42232C45D80"
        val cardSecret = "3134b62eebc255fec1b78b542428335e1bf597de8a7be8aff46371b1cc1be91d"

        val user = UserFixture.arbitrary(userId, aggregateVersion = aggregateVersion)
        val otherUser = UserFixture.withIdentity(UserIdentityFixture.card(cardId))

        // when
        val result = user.addCardIdentity(
            adminActor,
            fixedClock,
            correlationId,
            cardId,
            cardSecret
        ) {
            if (it == cardId) {
                otherUser.right()
            } else {
                throw IllegalArgumentException("unexpected card id")
            }
        }

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.CardIdAlreadyInUse(
                    "Card id is already in use.",
                    correlationId
                )
            )
    }

    @Test
    fun `given card identity when removing identity then expected sourcing event is returned`() {
        // given
        val cardId = "9E54A3187264EB"
        val identity = UserIdentityFixture.card(cardId)
        val user = UserFixture.withIdentity(identity, userId = userId, aggregateVersion = aggregateVersion)

        val expectedSourcingEvent = CardIdentityRemoved(
            userId,
            aggregateVersion + 1,
            adminActor.id,
            fixedInstant,
            correlationId,
            cardId = cardId
        )

        // when
        val result = user.removeCardIdentity(adminActor, fixedClock, correlationId, cardId)

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(expectedSourcingEvent)
    }

    @Test
    fun `given different card identity when removing identity then error is returned`() {
        // given
        val cardId = "9E54A3187264EB"
        val unknownCardId = "D4854276281ABD"

        val identity = UserIdentityFixture.card(cardId)
        val user = UserFixture.withIdentity(identity, userId = userId, aggregateVersion = aggregateVersion)

        // when
        val result = user.removeCardIdentity(adminActor, fixedClock, correlationId, unknownCardId)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.UserIdentityNotFound(
                    "Not able to find identity with card id $unknownCardId.",
                    mapOf(
                        "cardId" to unknownCardId
                    ),
                    correlationId
                )
            )
    }

    @Test
    fun `given no card identity when removing identity then error is returned`() {
        // given
        val unknownCardId = "7845B76EB036BB"

        val user = UserFixture.arbitrary(userId, identities = setOf())

        // when
        val result = user.removeCardIdentity(adminActor, fixedClock, correlationId, unknownCardId)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.UserIdentityNotFound(
                    "Not able to find identity with card id $unknownCardId.",
                    mapOf(
                        "cardId" to unknownCardId
                    ),
                    correlationId
                )
            )
    }

    @Test
    fun `when adding webauthn identity then expected sourcing event is returned`() {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = aggregateVersion)

        val authenticator = UserIdentityFixture.webauthn(byteArrayOf(1, 2, 3)).authenticator

        val expectedSourcingEvent = WebauthnIdentityAdded(
            userId,
            aggregateVersion + 1,
            userId,
            fixedInstant,
            correlationId,
            authenticator
        )

        // when
        val result = user.addWebauthnIdentity(
            user.asMember(),
            fixedClock,
            correlationId,
            authenticator
        )

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(expectedSourcingEvent)
    }

    @Test
    fun `given credential id is in use when adding webauthn identity then returns error`() {
        // given
        val existingIdentity = UserIdentityFixture.webauthn(byteArrayOf(1, 2, 3))
        val user = UserFixture.arbitrary(userId, identities = setOf(existingIdentity))

        // when
        val result = user.addWebauthnIdentity(
            user.asMember(),
            fixedClock,
            correlationId,
            existingIdentity.authenticator
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.CredentialIdAlreadyInUse(
                    "Credential id is already in use.",
                    correlationId
                )
            )
    }

    @Test
    fun `given user is not actor when adding webauthn identity then returns error`() {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = aggregateVersion)

        val authenticator = UserIdentityFixture.webauthn(byteArrayOf(1, 2, 3)).authenticator

        // when
        val result = user.addWebauthnIdentity(
            UserFixture.arbitrary().asMember(),
            fixedClock,
            correlationId,
            authenticator
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(Error.UserNotActor("User is not actor.", correlationId))
    }

    @Test
    fun `given credential id when removing webauthn identity then expected sourcing event is returned`() {
        // given
        val credentialId = byteArrayOf(1, 2, 3)
        val identity = UserIdentityFixture.webauthn(credentialId)
        val user = UserFixture.arbitrary(userId, identities = setOf(identity), aggregateVersion = aggregateVersion)

        val expectedSourcingEvent = WebauthnIdentityRemoved(
            userId,
            aggregateVersion + 1,
            adminActor.id,
            fixedInstant,
            correlationId,
            credentialId
        )

        // when
        val result = user.removeWebauthnIdentity(adminActor, fixedClock, correlationId, credentialId)

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(expectedSourcingEvent)
    }

    @Test
    fun `given different credential id when removing webauthn identity then returns error`() {
        // given
        val credentialId = byteArrayOf(1, 2, 3)
        val otherCredentialId = byteArrayOf(4, 5, 6)

        val identity = UserIdentityFixture.webauthn(credentialId)
        val user = UserFixture.arbitrary(userId, identities = setOf(identity))

        // when
        val result = user.removeWebauthnIdentity(
            adminActor,
            fixedClock,
            correlationId,
            otherCredentialId
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.UserIdentityNotFound(
                    "Not able to find identity with credentialId \"040506\".",
                    mapOf("credentialId" to "040506"),
                    correlationId
                )
            )
    }

    @Test
    fun `given no webauthn identity when removing webauthn identity then returns error`() {
        // given
        val credentialId = byteArrayOf(1, 2, 3)

        val user = UserFixture.arbitrary(userId, identities = setOf())

        // when
        val result = user.removeWebauthnIdentity(
            adminActor,
            fixedClock,
            correlationId,
            credentialId
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.UserIdentityNotFound(
                    "Not able to find identity with credentialId \"010203\".",
                    mapOf("credentialId" to "010203"),
                    correlationId
                )
            )
    }

    @Test
    fun `when adding phone number identity then expected sourcing event is returned`() = runTest {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = aggregateVersion)

        val phoneNr = "+491720000000"

        val expectedSourcingEvent = PhoneNrIdentityAdded(
            userId,
            aggregateVersion + 1,
            adminActor.id,
            fixedInstant,
            correlationId,
            phoneNr = phoneNr
        )

        // when
        val result = user.addPhoneNrIdentity(
            adminActor,
            fixedClock,
            correlationId,
            phoneNr
        ) {
            if (it == UserIdentityFixture.phoneNr(phoneNr)) {
                Error.UserNotFoundByIdentity("").left()
            } else {
                throw IllegalArgumentException("unexpected phone number")
            }
        }

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(expectedSourcingEvent)
    }

    @Test
    fun `given phone number is in use when adding phone number then returns error`() = runTest {
        // given
        val phoneNr = "+491720000123"
        val otherUser = UserFixture.withIdentity(UserIdentityFixture.phoneNr(phoneNr))

        val user = UserFixture.arbitrary(userId, aggregateVersion = aggregateVersion)

        // when
        val result = user.addPhoneNrIdentity(
            adminActor,
            fixedClock,
            correlationId,
            phoneNr
        ) {
            if (it == UserIdentityFixture.phoneNr(phoneNr)) {
                otherUser.right()
            } else {
                throw IllegalArgumentException("unexpected phone number")
            }
        }

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.PhoneNrAlreadyInUse(
                    "Phone number is already in use.",
                    correlationId
                )
            )
    }

    @Test
    fun `given phone number identity when removing identity then expected sourcing event is returned`() {
        // given
        val phoneNr = "+491230000000"
        val identity = UserIdentityFixture.phoneNr(phoneNr)
        val user = UserFixture.withIdentity(identity, userId = userId, aggregateVersion = aggregateVersion)

        val expectedSourcingEvent = PhoneNrIdentityRemoved(
            userId,
            aggregateVersion + 1,
            adminActor.id,
            fixedInstant,
            correlationId,
            phoneNr = phoneNr
        )

        // when
        val result = user.removePhoneNrIdentity(adminActor, fixedClock, correlationId, phoneNr)

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(expectedSourcingEvent)
    }

    @Test
    fun `given different phone number identity when removing identity then error is returned`() {
        // given
        val identity = UserIdentityFixture.phoneNr("+424242")
        val user = UserFixture.withIdentity(identity)

        // when
        val result = user.removePhoneNrIdentity(
            adminActor,
            fixedClock,
            correlationId,
            "+123"
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.UserIdentityNotFound(
                    "Not able to find identity with phone number +123.",
                    mapOf("phoneNr" to "+123"),
                    correlationId
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
            fixedClock,
            correlationId,
            "+4242123"
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.UserIdentityNotFound(
                    "Not able to find identity with phone number +4242123.",
                    mapOf("phoneNr" to "+4242123"),
                    correlationId
                )
            )
    }

    @Test
    fun `when adding pin identity then expected sourcing event is returned`() = runTest {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = aggregateVersion)

        val pin = "2468"

        val expectedSourcingEvent = PinIdentityAdded(
            userId,
            aggregateVersion = aggregateVersion + 1,
            adminActor.id,
            fixedInstant,
            correlationId,
            pin = pin
        )

        // when
        val result = user.addPinIdentity(
            adminActor,
            fixedClock,
            correlationId,
            pin
        )

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(expectedSourcingEvent)
    }

    @Test
    fun `given user already has pin identity already when adding pin identity then returns error`() = runTest {
        // given
        val identity = UserIdentityFixture.pin("1234")
        val user = UserFixture.arbitrary(userId, aggregateVersion = aggregateVersion, identities = setOf(identity))

        // when
        val result = user.addPinIdentity(
            adminActor,
            fixedClock,
            correlationId,
            "7890"
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.PinIdentityAlreadyFound(
                    "User already has a pin identity.",
                    correlationId
                )
            )
    }

    @Test
    fun `given pin identity when removing identity then expected sourcing event is returned`() = runTest {
        // given
        val pinIdentity = UserIdentityFixture.pin("8765")
        val user = UserFixture.withIdentity(pinIdentity, userId = userId, aggregateVersion = aggregateVersion)

        val expectedSourcingEvent = PinIdentityRemoved(
            userId,
            aggregateVersion + 1,
            adminActor.id,
            fixedInstant,
            correlationId
        )

        // when
        val result = user.removePinIdentity(adminActor, fixedClock, correlationId)

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(expectedSourcingEvent)
    }

    @Test
    fun `given no pin identity when removing identity then error is returned`() = runTest {
        // given
        val user = UserFixture.arbitrary(identities = setOf())

        // when
        val result = user.removePinIdentity(
            adminActor,
            fixedClock,
            correlationId
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.UserIdentityNotFound(
                    "Not able to find pin identity.",
                    mapOf(),
                    correlationId
                )
            )
    }

    @Nested
    internal inner class GivenInstructor {

        private val qualificationId = QualificationIdFixture.arbitrary()
        private val qualification = QualificationFixture.arbitrary(qualificationId)

        private val instructorActor: Instructor = InstructorFixture.arbitrary(qualifications = setOf(qualificationId))

        @Test
        fun `when adding member qualification then returns sourcing event`() = runTest {
            // given
            val user = UserFixture.arbitrary(userId, aggregateVersion = aggregateVersion)

            val expectedSourcingEvent = MemberQualificationAdded(
                userId,
                aggregateVersion + 1,
                instructorActor.id,
                fixedInstant,
                correlationId,
                qualificationId = qualificationId
            )

            // when
            val result = user.addMemberQualification(instructorActor, fixedClock, correlationId, qualificationId) {
                if (it == qualificationId) {
                    qualification.right()
                } else {
                    throw IllegalArgumentException("Unexpected qualification id")
                }
            }

            // then
            assertThat(result)
                .isRight()
                .isEqualTo(expectedSourcingEvent)
        }

        @Test
        fun `given already has qualification when adding member qualification then returns error`() = runTest {
            // given
            val user = UserFixture.arbitrary(
                userId,
                aggregateVersion = aggregateVersion,
                memberQualifications = setOf(qualificationId)
            )

            // when
            val result = user.addMemberQualification(instructorActor, fixedClock, correlationId, qualificationId) {
                throw IllegalStateException("should not get here")
            }

            // then
            assertThat(result)
                .isLeft()
                .isEqualTo(
                    Error.MemberQualificationAlreadyFound(
                        "User $userId already has member qualification $qualificationId.",
                        qualificationId,
                        correlationId
                    )
                )
        }

        @Test
        fun `given unknown qualification when adding member qualification then returns error`() = runTest {
            // given
            val user = UserFixture.arbitrary(userId, aggregateVersion = aggregateVersion)

            val error = ErrorFixture.arbitrary()

            // when
            val result = user.addMemberQualification(instructorActor, fixedClock, correlationId, qualificationId) {
                if (it == qualificationId) {
                    error.left()
                } else {
                    throw IllegalArgumentException("Unexpected qualification id")
                }
            }

            // then
            assertThat(result)
                .isLeft()
                .isEqualTo(error)
        }
    }

    @Test
    fun `given instructor not has qualification when adding member qualification then returns error`() = runTest {
        // given
        val qualificationId = QualificationIdFixture.arbitrary()
        val instructorActor: Instructor = InstructorFixture.arbitrary(qualifications = setOf())

        val user = UserFixture.arbitrary(userId, aggregateVersion = aggregateVersion)

        // when
        val result = user.addMemberQualification(instructorActor, fixedClock, correlationId, qualificationId) {
            throw IllegalStateException("should not get here")
        }

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.InstructorPermissionNotFound(
                    "Actor not has instructor permission for qualification $qualificationId.",
                    qualificationId,
                    correlationId
                )
            )
    }

    @Test
    fun `given has qualification when removing member qualification then returns sourcing event`() = runTest {
        // given
        val qualificationId = QualificationIdFixture.arbitrary()

        val user = UserFixture.arbitrary(
            userId,
            aggregateVersion = aggregateVersion,
            memberQualifications = setOf(qualificationId)
        )

        val expectedSourcingEvent = MemberQualificationRemoved(
            userId,
            aggregateVersion + 1,
            adminActor.id,
            fixedInstant,
            correlationId,
            qualificationId = qualificationId
        )

        // when
        val result = user.removeMemberQualification(adminActor, fixedClock, correlationId, qualificationId)

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(expectedSourcingEvent)
    }

    @Test
    fun `given triggered by domain event when removing member qualification then returns sourcing event`() {
        // given
        val qualificationId = QualificationIdFixture.arbitrary()

        val user = UserFixture.arbitrary(
            userId,
            aggregateVersion = aggregateVersion,
            memberQualifications = setOf(qualificationId)
        )

        val actorId = UserIdFixture.arbitrary()

        val domainEvent = QualificationDeleted(
            actorId,
            Clock.System.now(),
            correlationId,
            qualificationId
        )

        val expectedSourcingEvent = MemberQualificationRemoved(
            userId,
            aggregateVersion + 1,
            actorId,
            fixedInstant,
            correlationId,
            qualificationId = qualificationId
        )

        // when
        val result = user.removeMemberQualification(domainEvent, fixedClock, qualificationId)

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(expectedSourcingEvent)
    }

    @Test
    fun `given not has qualification when removing member qualification then returns error`() {
        // given
        val qualificationId = QualificationIdFixture.arbitrary()

        val user = UserFixture.arbitrary(
            userId,
            aggregateVersion = aggregateVersion,
            memberQualifications = setOf()
        )

        // when
        val result = user.removeMemberQualification(adminActor, fixedClock, correlationId, qualificationId)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.MemberQualificationNotFound(
                    "Not able to find member qualification with id $qualificationId.",
                    qualificationId,
                    correlationId
                )
            )
    }

    @Test
    fun `when adding instructor qualification then returns sourcing event`() = runTest {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = aggregateVersion)

        val qualificationId = QualificationIdFixture.arbitrary()
        val qualification = QualificationFixture.arbitrary(qualificationId)

        val expectedSourcingEvent = InstructorQualificationAdded(
            userId,
            aggregateVersion + 1,
            adminActor.id,
            fixedInstant,
            correlationId,
            qualificationId = qualificationId
        )

        // when
        val result = user.addInstructorQualification(adminActor, fixedClock, correlationId, qualificationId) {
            if (it == qualificationId) {
                qualification.right()
            } else {
                throw IllegalArgumentException("Unexpected qualification id")
            }
        }

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(expectedSourcingEvent)
    }

    @Test
    fun `given already has qualification when adding instructor qualification then returns error`() = runTest {
        // given
        val qualificationId = QualificationIdFixture.arbitrary()

        val user = UserFixture.arbitrary(
            userId,
            aggregateVersion = aggregateVersion,
            instructorQualifications = setOf(qualificationId)
        )

        // when
        val result = user.addInstructorQualification(adminActor, fixedClock, correlationId, qualificationId) {
            throw IllegalStateException("should not get here")
        }

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.InstructorQualificationAlreadyFound(
                    "User $userId already has instructor qualification $qualificationId.",
                    qualificationId,
                    correlationId
                )
            )
    }

    @Test
    fun `given unknown qualification when adding instructor qualification then returns error`() = runTest {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = aggregateVersion)

        val unknownQualificationId = QualificationIdFixture.arbitrary()

        val error = ErrorFixture.arbitrary()

        // when
        val result = user.addInstructorQualification(adminActor, fixedClock, correlationId, unknownQualificationId) {
            if (it == unknownQualificationId) {
                error.left()
            } else {
                throw IllegalArgumentException("Unexpected qualification id")
            }
        }

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }

    @Test
    fun `given has qualification when removing instructor qualification then returns sourcing event`() {
        // given
        val qualificationId = QualificationIdFixture.arbitrary()

        val user = UserFixture.arbitrary(
            userId,
            aggregateVersion = aggregateVersion,
            instructorQualifications = setOf(qualificationId)
        )

        val expectedSourcingEvent = InstructorQualificationRemoved(
            userId,
            aggregateVersion + 1,
            adminActor.id,
            fixedInstant,
            correlationId,
            qualificationId = qualificationId
        )

        // when
        val result = user.removeInstructorQualification(adminActor, fixedClock, correlationId, qualificationId)

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(expectedSourcingEvent)
    }

    @Test
    fun `given triggered by domain event when removing instructor qualification then returns sourcing event`() {
        // given
        val qualificationId = QualificationIdFixture.arbitrary()

        val user = UserFixture.arbitrary(
            userId,
            aggregateVersion = aggregateVersion,
            instructorQualifications = setOf(qualificationId)
        )

        val actorId = UserIdFixture.arbitrary()

        val domainEvent = QualificationDeleted(
            actorId,
            Clock.System.now(),
            correlationId,
            qualificationId
        )

        val expectedSourcingEvent = InstructorQualificationRemoved(
            userId,
            aggregateVersion + 1,
            actorId,
            fixedInstant,
            correlationId,
            qualificationId = qualificationId
        )

        // when
        val result = user.removeInstructorQualification(domainEvent, fixedClock, qualificationId)

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(expectedSourcingEvent)
    }

    @Test
    fun `given not has qualification when removing instructor qualification then returns error`() {
        // given
        val qualificationId = QualificationIdFixture.arbitrary()

        val user = UserFixture.arbitrary(
            userId,
            aggregateVersion = aggregateVersion,
            instructorQualifications = null
        )

        // when
        val result = user.removeInstructorQualification(adminActor, fixedClock, correlationId, qualificationId)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.InstructorQualificationNotFound(
                    "Not able to find instructor qualification with id $qualificationId.",
                    qualificationId,
                    correlationId
                )
            )
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `when changing isAdmin then returns sourcing event`(
        changeTo: Boolean
    ) {
        // given
        val user = UserFixture.arbitrary(
            userId,
            aggregateVersion = aggregateVersion,
            isAdmin = !changeTo
        )

        val expectedSourcingEvent = IsAdminChanged(
            userId,
            aggregateVersion + 1,
            adminActor.id,
            fixedInstant,
            correlationId,
            changeTo
        )

        // when
        val result = user.changeIsAdmin(adminActor, fixedClock, correlationId, changeTo)

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(expectedSourcingEvent)
    }

    @Test
    fun `given user already is admin when changing isAdmin then returns error`() {
        // given
        val user = UserFixture.arbitrary(
            userId,
            aggregateVersion = aggregateVersion,
            isAdmin = true
        )

        // when
        val result = user.changeIsAdmin(adminActor, fixedClock, correlationId, true)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(Error.UserAlreadyAdmin("User already is admin.", correlationId))
    }

    @Test
    fun `given user already is not admin when changing isAdmin then returns error`() {
        // given
        val user = UserFixture.arbitrary(
            userId,
            aggregateVersion = aggregateVersion,
            isAdmin = false
        )

        // when
        val result = user.changeIsAdmin(adminActor, fixedClock, correlationId, false)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(Error.UserAlreadyNotAdmin("User already not is admin.", correlationId))
    }

    @Test
    fun `when deleting then expected sourcing event is returned`() {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = aggregateVersion)

        val expectedSourcingEvent = UserDeleted(
            userId,
            aggregateVersion + 1,
            adminActor.id,
            fixedInstant,
            correlationId,
        )

        // when
        val result = user.delete(adminActor, fixedClock, correlationId)

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(expectedSourcingEvent)
    }

    @Test
    fun `given user is actor when deleting then returns error`() {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = aggregateVersion)

        val actor = Admin(userId, user.wikiName)

        // when
        val result = user.delete(actor, fixedClock, correlationId)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(Error.UserIsActor("User is actor and cannot delete themselves.", correlationId))
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "user123, 4Mt7/bEfN/samfzYogSTVcnCleWI1zehSJa4HdcWsMQ=, true",
            "user123, AAAA/bEfN/samfzYogSTVcnCleWI1zehSJa4HdcWsMQ=, false",
            "user,    4Mt7/bEfN/samfzYogSTVcnCleWI1zehSJa4HdcWsMQ=, false",
            "user,    AAAA/bEfN/samfzYogSTVcnCleWI1zehSJa4HdcWsMQ=,   false",
        ]
    )
    fun `given user with username password identity when checking hasIdentity then returns expected result`(
        username: String,
        password: String,
        expectedResult: Boolean
    ) {
        // given
        val identity = UserIdentityFixture.usernamePassword("user123", "4Mt7/bEfN/samfzYogSTVcnCleWI1zehSJa4HdcWsMQ=")
        val user = UserFixture.withIdentity(identity)

        // when
        val result = user.hasIdentity(UserIdentityFixture.usernamePassword(username, password))

        // then
        assertThat(result).isEqualTo(expectedResult)
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "E272BFA478B021, 10250DC83A755C8FFD0FC6249E231B62E1E983849BBA4BB6BF8D5EB75DDD2B29, true",
            "AAAAAAAAAAAAAA, 10250DC83A755C8FFD0FC6249E231B62E1E983849BBA4BB6BF8D5EB75DDD2B29, false",
            "E272BFA478B021, BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB, false",
            "AAAAAAAAAAAAAA, BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB, false",
        ]
    )
    fun `given user with card identity when checking hasIdentity then returns expected result`(
        cardId: String,
        cardSecret: String,
        expectedResult: Boolean
    ) {
        // given
        val identity = UserIdentityFixture.card(
            "E272BFA478B021",
            "10250DC83A755C8FFD0FC6249E231B62E1E983849BBA4BB6BF8D5EB75DDD2B29"
        )
        val user = UserFixture.withIdentity(identity)

        // when
        val result = user.hasIdentity(UserIdentityFixture.card(cardId, cardSecret))

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
        val identity = UserIdentityFixture.phoneNr("+4912300000042")
        val user = UserFixture.withIdentity(identity)

        // when
        val result = user.hasIdentity(UserIdentityFixture.phoneNr(phoneNr))

        // then
        assertThat(result).isEqualTo(expectedResult)
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "name1, name1, true",
            "longername, longername, true",
            "name1, name2, false"
        ]
    )
    fun `given user with username password identity when checking hasUsername then returns expected result`(
        identityUsername: String, checkUsername: String, expectedResult: Boolean
    ) {
        // given
        val identity = UserIdentityFixture.usernamePassword(identityUsername)
        val user = UserFixture.withIdentity(identity)

        // when
        val result = user.hasUsername(checkUsername)

        // then
        assertThat(result).isEqualTo(expectedResult)
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "100DF3CE80B5F8, 100DF3CE80B5F8, true",
            "00000000000001, 00000000000001, true",
            "100DF3CE80B5F8, 11111111111111, false"
        ]
    )
    fun `given user with card identity when checking hasCardId then returns expected result`(
        identityCardId: String, checkCardId: String, expectedResult: Boolean
    ) {
        // given
        val identity = UserIdentityFixture.card(identityCardId)
        val user = UserFixture.withIdentity(identity)

        // when
        val result = user.hasCardId(checkCardId)

        // then
        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `given any user when getting as member then returns member`() {
        // given
        val qualifications = setOf(QualificationIdFixture.arbitrary(), QualificationIdFixture.arbitrary())
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
        val qualification1 = QualificationIdFixture.arbitrary()
        val qualification2 = QualificationIdFixture.arbitrary()

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
            UserIdFixture.static(42),
            123,
            "Nikola",
            "Tesla",
            "nick",
            false,
            null,
            setOf(),
            setOf(QualificationIdFixture.static(43)),
            setOf(QualificationIdFixture.static(44)),
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