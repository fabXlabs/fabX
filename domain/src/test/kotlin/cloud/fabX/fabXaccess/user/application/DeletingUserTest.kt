package cloud.fabX.fabXaccess.user.application

import arrow.core.None
import arrow.core.left
import arrow.core.right
import arrow.core.some
import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.user.model.AdminFixture
import cloud.fabX.fabXaccess.user.model.UserDeleted
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import cloud.fabX.fabXaccess.user.model.UserRepository
import isNone
import isSome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.whenever

@MockitoSettings
internal class DeletingUserTest {

    private val adminActor = AdminFixture.arbitrary()

    private val userId = UserIdFixture.arbitraryId()

    private var logger: Logger? = null
    private var userRepository: UserRepository? = null

    private var testee: DeletingUser? = null

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock userRepository: UserRepository
    ) {
        this.logger = logger
        this.userRepository = userRepository
        DomainModule.configureLoggerFactory { logger }
        DomainModule.configureUserRepository(userRepository)

        testee = DeletingUser()
    }

    @Test
    fun `given user can be found when deleting user then sourcing event is created and stored`() {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = 987)

        val expectedSourcingEvent = UserDeleted(
            userId,
            988,
            adminActor.id
        )

        whenever(userRepository!!.getById(userId))
            .thenReturn(user.right())

        whenever(userRepository!!.store(expectedSourcingEvent))
            .thenReturn(None)

        // when
        val result = testee!!.deleteUser(
            adminActor,
            userId
        )

        // then
        assertThat(result).isNone()
    }

    @Test
    fun `given user cannot be found when deleting user then returns error`() {
        // given
        val error = Error.UserNotFound("message", userId)

        whenever(userRepository!!.getById(userId))
            .thenReturn(error.left())

        // when
        val result = testee!!.deleteUser(
            adminActor,
            userId
        )

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(error)
    }

    @Test
    fun `given sourcing event cannot be stored when deleting user then returns error`() {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = 987)

        val event = UserDeleted(
            userId,
            988,
            adminActor.id
        )

        val error = Error.UserNotFound("message", userId)

        whenever(userRepository!!.getById(userId))
            .thenReturn(user.right())

        whenever(userRepository!!.store(event))
            .thenReturn(error.some())

        // when
        val result = testee!!.deleteUser(
            adminActor,
            userId
        )

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(error)
    }
}