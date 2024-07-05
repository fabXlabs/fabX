package cloud.fabX.fabXaccess.user.application

import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isSameInstanceAs
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.ErrorFixture
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.model.newCorrelationId
import cloud.fabX.fabXaccess.common.model.newUserId
import cloud.fabX.fabXaccess.user.model.AdminFixture
import cloud.fabX.fabXaccess.user.model.UserCreated
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import cloud.fabX.fabXaccess.user.model.UserPersonalInformationChanged
import cloud.fabX.fabXaccess.user.model.UserRepository
import isLeft
import isRight
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.whenever

@MockitoSettings
class GettingUserSourcingEventsTest {

    private val adminActor = AdminFixture.arbitrary()
    private val userId = UserIdFixture.arbitrary()
    private val correlationId = CorrelationIdFixture.arbitrary()

    private lateinit var logger: Logger
    private lateinit var userRepository: UserRepository

    private lateinit var testee: GettingUserSourcingEvents

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock userRepository: UserRepository
    ) {
        this.logger = logger
        this.userRepository = userRepository

        testee = GettingUserSourcingEvents({ logger }, userRepository)
    }

    @Test
    fun `when getting all sourcing events then returns all from repository`() = runTest {
        // given
        val sourcingEvents = listOf(
            UserCreated(
                aggregateRootId = newUserId(),
                actorId = newUserId(),
                timestamp = Instant.fromEpochSeconds(1720088380, 0),
                newCorrelationId(),
                "hello",
                "world",
                "hello.world"
            ),
            UserCreated(
                aggregateRootId = newUserId(),
                actorId = newUserId(),
                timestamp = Instant.fromEpochSeconds(1720088381, 0),
                newCorrelationId(),
                "some",
                "body",
                "some.body"
            ),
        )

        whenever(userRepository.getSourcingEvents())
            .thenReturn(sourcingEvents)

        // when
        val result = testee.getAll(adminActor, correlationId)

        // then
        assertThat(result)
            .isSameInstanceAs(sourcingEvents)
    }

    @Test
    fun `when getting sourcing events by id then returns sourcing events from repository`() = runTest {
        // given
        val sourcingEvents = listOf(
            UserCreated(
                aggregateRootId = userId,
                actorId = newUserId(),
                timestamp = Instant.fromEpochSeconds(1720088380, 0),
                newCorrelationId(),
                "hello",
                "world",
                "hello.world"
            ),
            UserPersonalInformationChanged(
                aggregateRootId = userId,
                aggregateVersion = 2,
                actorId = newUserId(),
                timestamp = Instant.fromEpochSeconds(1720088381, 0),
                correlationId = newCorrelationId(),
                firstName = ChangeableValue.ChangeToValueString("hi"),
                lastName = ChangeableValue.LeaveAsIs,
                wikiName = ChangeableValue.LeaveAsIs
            )
        )

        whenever(userRepository.getSourcingEventsById(userId))
            .thenReturn(sourcingEvents.right())

        // when
        val result = testee.getById(adminActor, correlationId, userId)

        // then
        assertThat(result)
            .isRight()
            .isSameInstanceAs(sourcingEvents)
    }

    @Test
    fun `given repository error when getting sourcing events by id then returns error`() = runTest {
        // given
        val expectedError = ErrorFixture.arbitrary()

        whenever(userRepository.getSourcingEventsById(userId))
            .thenReturn(expectedError.left())

        // when
        val result = testee.getById(adminActor, correlationId, userId)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(expectedError)
    }
}