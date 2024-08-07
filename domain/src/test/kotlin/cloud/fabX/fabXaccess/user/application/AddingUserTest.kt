package cloud.fabX.fabXaccess.user.application

import FixedClock
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.ErrorFixture
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.user.model.AdminFixture
import cloud.fabX.fabXaccess.user.model.GettingUserByWikiName
import cloud.fabX.fabXaccess.user.model.UserCreated
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import cloud.fabX.fabXaccess.user.model.UserRepository
import isLeft
import isRight
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.whenever

@MockitoSettings
internal class AddingUserTest {

    private val adminActor = AdminFixture.arbitrary()
    private val correlationId = CorrelationIdFixture.arbitrary()

    private val userId = UserIdFixture.arbitrary()

    private val fixedInstant = Clock.System.now()
    private val fixedClock = FixedClock(fixedInstant)

    private lateinit var logger: Logger
    private lateinit var userRepository: UserRepository
    private lateinit var gettingUserByWikiName: GettingUserByWikiName

    private lateinit var testee: AddingUser

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock userRepository: UserRepository,
        @Mock gettingUserByWikiName: GettingUserByWikiName
    ) {
        this.logger = logger
        this.userRepository = userRepository
        this.gettingUserByWikiName = gettingUserByWikiName

        testee = AddingUser({ logger }, userRepository, gettingUserByWikiName, { userId }, fixedClock)
    }

    @Test
    fun `given valid values when adding user then sourcing event is created and stored`() = runTest {
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

        whenever(gettingUserByWikiName.getByWikiName(wikiName))
            .thenReturn(Error.UserNotFoundByWikiName("").left())

        whenever(userRepository.store(expectedSourcingEvent))
            .thenReturn(Unit.right())

        // when
        val result = testee.addUser(
            adminActor,
            correlationId,
            firstName,
            lastName,
            wikiName
        )

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(userId)
    }

    @Test
    fun `given domain error when adding user then returns domain error`() = runTest {
        // given
        val wikiName = "wiki"
        val otherUser = UserFixture.arbitrary(wikiName = wikiName)

        whenever(gettingUserByWikiName.getByWikiName(wikiName))
            .thenReturn(otherUser.right())

        val expectedDomainError = Error.WikiNameAlreadyInUse("Wiki name is already in use.", correlationId)

        // when
        val result = testee.addUser(
            adminActor,
            correlationId,
            "first",
            "last",
            wikiName
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(expectedDomainError)
    }

    @Test
    fun `given sourcing event cannot be stored when adding user then returns error`() = runTest {
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

        val error = ErrorFixture.arbitrary()

        whenever(gettingUserByWikiName.getByWikiName(wikiName))
            .thenReturn(Error.UserNotFoundByWikiName("").left())

        whenever(userRepository.store(expectedSourcingEvent))
            .thenReturn(error.left())

        // when
        val result = testee.addUser(
            adminActor,
            correlationId,
            firstName,
            lastName,
            wikiName
        )

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(error)
    }
}