package cloud.fabX.fabXaccess.user.model

import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.model.AggregateVersionDoesNotIncreaseOneByOne
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.ErrorFixture
import cloud.fabX.fabXaccess.common.model.IterableIsEmpty
import cloud.fabX.fabXaccess.common.model.QualificationDeleted
import cloud.fabX.fabXaccess.qualification.model.QualificationFixture
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFixture
import isLeft
import isNone
import isRight
import isSome
import kotlinx.datetime.Clock
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource

internal class UserTest {

    private val adminActor = AdminFixture.arbitrary()
    private val correlationId = CorrelationIdFixture.arbitrary()

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
    fun `when adding new user then returns expected sourcing event`() {
        // given
        DomainModule.configureUserIdFactory { userId }

        val firstName = "first"
        val lastName = "last"
        val wikiName = "wiki"

        val expectedSourcingEvent = UserCreated(
            userId,
            adminActor.id,
            correlationId,
            firstName,
            lastName,
            wikiName
        )

        // when
        val result = User.addNew(
            adminActor,
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
    fun `given wiki name is already in use when adding new user then returns error`() {
        // given
        val otherUser = UserFixture.arbitrary(wikiName = "someone")

        // when
        val result = User.addNew(
            adminActor,
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
                Error.WikiNameAlreadyInUse("Wiki name is already in use.")
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
            CorrelationIdFixture.arbitrary(),
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
            CorrelationIdFixture.arbitrary(),
            firstName = "first1",
            lastName = "last1",
            wikiName = "wiki1"
        )
        val event2 = UserPersonalInformationChanged(
            userId,
            2,
            adminActor.id,
            CorrelationIdFixture.arbitrary(),
            firstName = ChangeableValue.ChangeToValue("first2"),
            lastName = ChangeableValue.ChangeToValue("last2"),
            wikiName = ChangeableValue.ChangeToValue("wiki2")
        )
        val event3 = UserLockStateChanged(
            userId,
            3,
            adminActor.id,
            CorrelationIdFixture.arbitrary(),
            locked = ChangeableValue.ChangeToValue(true),
            notes = ChangeableValue.ChangeToValue("some notes")
        )
        val event4 = UserPersonalInformationChanged(
            userId,
            4,
            adminActor.id,
            CorrelationIdFixture.arbitrary(),
            firstName = ChangeableValue.ChangeToValue("first4"),
            lastName = ChangeableValue.LeaveAsIs,
            wikiName = ChangeableValue.ChangeToValue("wiki4")
        )
        val event5 = UserPersonalInformationChanged(
            userId,
            5,
            adminActor.id,
            CorrelationIdFixture.arbitrary(),
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
            CorrelationIdFixture.arbitrary(),
            firstName = "first1",
            lastName = "last1",
            wikiName = "wiki1"
        )
        val event3 = UserPersonalInformationChanged(
            userId,
            3,
            adminActor.id,
            CorrelationIdFixture.arbitrary(),
            firstName = ChangeableValue.ChangeToValue("first2"),
            lastName = ChangeableValue.LeaveAsIs,
            wikiName = ChangeableValue.ChangeToValue("wiki2")
        )
        val event2 = UserLockStateChanged(
            userId,
            2,
            adminActor.id,
            CorrelationIdFixture.arbitrary(),
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
            CorrelationIdFixture.arbitrary(),
            qualificationId1
        )

        val event2 = MemberQualificationAdded(
            userId,
            3,
            adminActor.id,
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
            CorrelationIdFixture.arbitrary(),
            qualificationId1
        )

        val event2 = InstructorQualificationAdded(
            userId,
            3,
            adminActor.id,
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
            CorrelationIdFixture.arbitrary(),
            qualificationId1
        )

        val event2 = InstructorQualificationRemoved(
            userId,
            43,
            adminActor.id,
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
            CorrelationIdFixture.arbitrary(),
            firstName = "first1",
            lastName = "last1",
            wikiName = "wiki1"
        )

        val event2 = UserDeleted(
            userId,
            2,
            adminActor.id,
            CorrelationIdFixture.arbitrary(),
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
            correlationId = correlationId,
            firstName = ChangeableValue.ChangeToValue("newFistName"),
            lastName = ChangeableValue.LeaveAsIs,
            wikiName = ChangeableValue.ChangeToValue("newWikiName")
        )

        // when
        val result = user.changePersonalInformation(
            actor = adminActor,
            correlationId = correlationId,
            firstName = ChangeableValue.ChangeToValue("newFistName"),
            lastName = ChangeableValue.LeaveAsIs,
            wikiName = ChangeableValue.ChangeToValue("newWikiName")
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
    fun `given wiki name is already in use when changing personal information then returns error`() {
        // given
        val otherUser = UserFixture.arbitrary(wikiName = "wikiName")

        val user = UserFixture.arbitrary(userId, aggregateVersion = aggregateVersion)

        // when
        val result = user.changePersonalInformation(
            adminActor,
            correlationId,
            wikiName = ChangeableValue.ChangeToValue("wikiName")
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
            .isEqualTo(Error.WikiNameAlreadyInUse("Wiki name is already in use."))
    }

    @Test
    fun `when changing lock state then expected sourcing event is returned`() {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = aggregateVersion)

        val expectedSourcingEvent = UserLockStateChanged(
            actorId = adminActor.id,
            correlationId = correlationId,
            aggregateRootId = userId,
            aggregateVersion = aggregateVersion + 1,
            locked = ChangeableValue.ChangeToValue(true),
            notes = ChangeableValue.ChangeToValue(null)
        )

        // when
        val result = user.changeLockState(
            actor = adminActor,
            correlationId = correlationId,
            locked = ChangeableValue.ChangeToValue(true),
            notes = ChangeableValue.ChangeToValue(null)
        )

        // then
        assertThat(result).isEqualTo(expectedSourcingEvent)
    }

    @Test
    fun `when adding username password identity then expected sourcing event is returned`() {
        // given
        val user = UserFixture.arbitrary(
            userId,
            aggregateVersion = aggregateVersion,
            identities = setOf(UserIdentityFixture.card())
        )

        val username = "name42"
        val hash = "Q1kxQjCP56+cO62gMvOP+T9gz0uEnZIOnzT7TeJ21V4="

        val expectedSourcingEvent = UsernamePasswordIdentityAdded(
            actorId = adminActor.id,
            correlationId = correlationId,
            aggregateRootId = userId,
            aggregateVersion = aggregateVersion + 1,
            username = username,
            hash = hash
        )

        // when
        val result = user.addUsernamePasswordIdentity(
            adminActor,
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
    fun `given user already has username password identity when adding username password identity then returns error`() {
        // given
        val identity = UserIdentityFixture.usernamePassword("username")
        val user = UserFixture.arbitrary(userId, aggregateVersion = aggregateVersion, identities = setOf(identity))

        // when
        val result = user.addUsernamePasswordIdentity(
            adminActor,
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
                Error.UsernamePasswordIdentityAlreadyFound("User already has a username password identity.")
            )
    }

    @Test
    fun `given username is already in use when adding username password identity then returns error`() {
        // given
        val otherUser = UserFixture.arbitrary(
            identities = setOf(UserIdentityFixture.usernamePassword("name"))
        )

        val user = UserFixture.arbitrary(userId, aggregateVersion = aggregateVersion, identities = setOf())

        // when
        val result = user.addUsernamePasswordIdentity(
            adminActor,
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
                    "Username is already in use."
                )
            )

    }

    @Test
    fun `given username password identity when removing identity then expected sourcing event is returned`() {
        // given
        val username = "u1"
        val identity = UserIdentityFixture.usernamePassword(username)
        val user = UserFixture.withIdentity(identity, userId = userId, aggregateVersion = aggregateVersion)

        val expectedSourcingEvent = UsernamePasswordIdentityRemoved(
            actorId = adminActor.id,
            correlationId = correlationId,
            aggregateRootId = userId,
            aggregateVersion = aggregateVersion + 1,
            username = username
        )

        // when
        val result = user.removeUsernamePasswordIdentity(adminActor, correlationId, username)

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(expectedSourcingEvent)
    }

    @Test
    fun `given different username password identity when removing identity then error is returned`() {
        // given
        val identity = UserIdentityFixture.usernamePassword("u1")
        val user = UserFixture.withIdentity(identity)

        // when
        val result = user.removeUsernamePasswordIdentity(
            adminActor,
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
                    )
                )
            )
    }

    @Test
    fun `when adding card identity then expected sourcing event is returned`() {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = aggregateVersion)

        val cardId = "02E42232C45D80"
        val cardSecret = "D7673E42DA311BFF83EBD0761ED8CD607200A9E0F6065E6B945AE86D14EAB3C6"

        val expectedSourcingEvent = CardIdentityAdded(
            actorId = adminActor.id,
            correlationId = correlationId,
            aggregateRootId = userId,
            aggregateVersion = aggregateVersion + 1,
            cardId = cardId,
            cardSecret = cardSecret
        )

        // when
        val result = user.addCardIdentity(
            adminActor,
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
    fun `given card id is in use when adding card identity then returns error`() {
        // given
        val cardId = "02E42232C45D80"
        val cardSecret = "3134b62eebc255fec1b78b542428335e1bf597de8a7be8aff46371b1cc1be91d"

        val user = UserFixture.arbitrary(userId, aggregateVersion = aggregateVersion)
        val otherUser = UserFixture.withIdentity(UserIdentityFixture.card(cardId))

        // when
        val result = user.addCardIdentity(
            adminActor,
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
                    "Card id is already in use."
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
            actorId = adminActor.id,
            correlationId = correlationId,
            aggregateRootId = userId,
            aggregateVersion = aggregateVersion + 1,
            cardId = cardId
        )

        // when
        val result = user.removeCardIdentity(adminActor, correlationId, cardId)

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
        val result = user.removeCardIdentity(adminActor, correlationId, unknownCardId)

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
        val unknownCardId = "7845B76EB036BB"

        val user = UserFixture.arbitrary(userId, identities = setOf())

        // when
        val result = user.removeCardIdentity(adminActor, correlationId, unknownCardId)

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
            correlationId = correlationId,
            aggregateRootId = userId,
            aggregateVersion = aggregateVersion + 1,
            phoneNr = phoneNr
        )

        // when
        val result = user.addPhoneNrIdentity(
            adminActor,
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
    fun `given phone number is in use when adding phone number then returns error`() {
        // given
        val phoneNr = "+491720000123"
        val otherUser = UserFixture.withIdentity(UserIdentityFixture.phoneNr(phoneNr))

        val user = UserFixture.arbitrary(userId, aggregateVersion = aggregateVersion)

        // when
        val result = user.addPhoneNrIdentity(
            adminActor,
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
                    "Phone number is already in use."
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
            actorId = adminActor.id,
            correlationId = correlationId,
            aggregateRootId = userId,
            aggregateVersion = aggregateVersion + 1,
            phoneNr = phoneNr
        )

        // when
        val result = user.removePhoneNrIdentity(adminActor, correlationId, phoneNr)

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
            correlationId,
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
            correlationId,
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

    @Nested
    internal inner class GivenInstructor {

        private val qualificationId = QualificationIdFixture.arbitrary()
        private val qualification = QualificationFixture.arbitrary(qualificationId)

        private val instructorActor: Instructor = InstructorFixture.arbitrary(qualifications = setOf(qualificationId))

        @Test
        fun `when adding member qualification then returns sourcing event`() {
            // given
            val user = UserFixture.arbitrary(userId, aggregateVersion = aggregateVersion)

            val expectedSourcingEvent = MemberQualificationAdded(
                aggregateRootId = userId,
                correlationId = correlationId,
                aggregateVersion = aggregateVersion + 1,
                actorId = instructorActor.id,
                qualificationId = qualificationId
            )

            // when
            val result = user.addMemberQualification(instructorActor, correlationId, qualificationId) {
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
        fun `given already has qualification when adding member qualification then returns error`() {
            // given
            val user = UserFixture.arbitrary(
                userId,
                aggregateVersion = aggregateVersion,
                memberQualifications = setOf(qualificationId)
            )

            // when
            val result = user.addMemberQualification(instructorActor, correlationId, qualificationId) {
                throw IllegalStateException("should not get here")
            }

            // then
            assertThat(result)
                .isLeft()
                .isEqualTo(
                    Error.MemberQualificationAlreadyFound(
                        "User $userId already has member qualification $qualificationId.",
                        qualificationId
                    )
                )
        }

        @Test
        fun `given unknown qualification when adding member qualification then returns error`() {
            // given
            val user = UserFixture.arbitrary(userId, aggregateVersion = aggregateVersion)

            val error = ErrorFixture.arbitrary()

            // when
            val result = user.addMemberQualification(instructorActor, correlationId, qualificationId) {
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
    fun `given instructor not has qualification when adding member qualification then returns error`() {
        // given
        val qualificationId = QualificationIdFixture.arbitrary()
        val instructorActor: Instructor = InstructorFixture.arbitrary(qualifications = setOf())

        val user = UserFixture.arbitrary(userId, aggregateVersion = aggregateVersion)

        // when
        val result = user.addMemberQualification(instructorActor, correlationId, qualificationId) {
            throw IllegalStateException("should not get here")
        }

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.InstructorPermissionNotFound(
                    "Actor not has instructor permission for qualification $qualificationId.",
                    qualificationId
                )
            )
    }

    @Test
    fun `given has qualification when removing member qualification then returns sourcing event`() {
        // given
        val qualificationId = QualificationIdFixture.arbitrary()

        val user = UserFixture.arbitrary(
            userId,
            aggregateVersion = aggregateVersion,
            memberQualifications = setOf(qualificationId)
        )

        val expectedSourcingEvent = MemberQualificationRemoved(
            aggregateRootId = userId,
            aggregateVersion = aggregateVersion + 1,
            actorId = adminActor.id,
            correlationId = correlationId,
            qualificationId = qualificationId
        )

        // when
        val result = user.removeMemberQualification(adminActor, correlationId, qualificationId)

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
            aggregateRootId = userId,
            aggregateVersion = aggregateVersion + 1,
            actorId = actorId,
            correlationId = correlationId,
            qualificationId = qualificationId
        )

        // when
        val result = user.removeMemberQualification(domainEvent, qualificationId)

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
        val result = user.removeMemberQualification(adminActor, correlationId, qualificationId)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.MemberQualificationNotFound(
                    "Not able to find member qualification with id $qualificationId.",
                    qualificationId
                )
            )
    }

    @Test
    fun `when adding instructor qualification then returns sourcing event`() {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = aggregateVersion)

        val qualificationId = QualificationIdFixture.arbitrary()
        val qualification = QualificationFixture.arbitrary(qualificationId)

        val expectedSourcingEvent = InstructorQualificationAdded(
            aggregateRootId = userId,
            aggregateVersion = aggregateVersion + 1,
            actorId = adminActor.id,
            correlationId = correlationId,
            qualificationId = qualificationId
        )

        // when
        val result = user.addInstructorQualification(adminActor, correlationId, qualificationId) {
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
    fun `given already has qualification when adding instructor qualification then returns error`() {
        // given
        val qualificationId = QualificationIdFixture.arbitrary()

        val user = UserFixture.arbitrary(
            userId,
            aggregateVersion = aggregateVersion,
            instructorQualifications = setOf(qualificationId)
        )

        // when
        val result = user.addInstructorQualification(adminActor, correlationId, qualificationId) {
            throw IllegalStateException("should not get here")
        }

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.InstructorQualificationAlreadyFound(
                    "User $userId already has instructor qualification $qualificationId.",
                    qualificationId
                )
            )
    }

    @Test
    fun `given unknown qualification when adding instructor qualification then returns error`() {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = aggregateVersion)

        val unknownQualificationId = QualificationIdFixture.arbitrary()

        val error = ErrorFixture.arbitrary()

        // when
        val result = user.addInstructorQualification(adminActor, correlationId, unknownQualificationId) {
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
            aggregateRootId = userId,
            aggregateVersion = aggregateVersion + 1,
            actorId = adminActor.id,
            correlationId = correlationId,
            qualificationId = qualificationId
        )

        // when
        val result = user.removeInstructorQualification(adminActor, correlationId, qualificationId)

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
            aggregateRootId = userId,
            aggregateVersion = aggregateVersion + 1,
            actorId = actorId,
            correlationId = correlationId,
            qualificationId = qualificationId
        )

        // when
        val result = user.removeInstructorQualification(domainEvent, qualificationId)

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
        val result = user.removeInstructorQualification(adminActor, correlationId, qualificationId)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.InstructorQualificationNotFound(
                    "Not able to find instructor qualification with id $qualificationId.",
                    qualificationId
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
            correlationId,
            changeTo
        )

        // when
        val result = user.changeIsAdmin(adminActor, correlationId, changeTo)

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
        val result = user.changeIsAdmin(adminActor, correlationId, true)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(Error.UserAlreadyAdmin("User already is admin."))
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
        val result = user.changeIsAdmin(adminActor, correlationId, false)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(Error.UserAlreadyNotAdmin("User already not is admin."))
    }

    @Test
    fun `when deleting then expected sourcing event is returned`() {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = aggregateVersion)

        val expectedSourcingEvent = UserDeleted(
            aggregateRootId = userId,
            aggregateVersion = aggregateVersion + 1,
            actorId = adminActor.id,
            correlationId = correlationId,
        )

        // when
        val result = user.delete(adminActor, correlationId)

        // then
        assertThat(result).isEqualTo(expectedSourcingEvent)
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