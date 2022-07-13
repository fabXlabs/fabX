package cloud.fabX.fabXaccess.user.application

import arrow.core.None
import arrow.core.some
import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.model.ErrorFixture
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.user.model.AdminFixture
import cloud.fabX.fabXaccess.user.model.UserCreated
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
internal class AddingUserTest {

    private val adminActor = AdminFixture.arbitraryAdmin()

    private val userId = UserIdFixture.arbitraryId()

    private var logger: Logger? = null
    private var userRepository: UserRepository? = null

    private var testee: AddingUser? = null

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock userRepository: UserRepository
    ) {
        this.logger = logger
        this.userRepository = userRepository

        DomainModule.configureLoggerFactory { logger }
        DomainModule.configureUserIdFactory { userId }
        DomainModule.configureUserRepository(userRepository)

        testee = AddingUser()
    }

    @Test
    fun `given valid values when adding user then sourcing event is created and stored`() {
        // given
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

        whenever(userRepository!!.store(expectedSourcingEvent))
            .thenReturn(None)

        // when
        val result = testee!!.addUser(
            adminActor,
            firstName,
            lastName,
            wikiName
        )

        // then
        assertThat(result).isNone()
    }

    @Test
    fun `given sourcing event cannot be stored when adding user then returns error`() {
        // given
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

        val error = ErrorFixture.arbitraryError()

        whenever(userRepository!!.store(expectedSourcingEvent))
            .thenReturn(error.some())

        // when
        val result = testee!!.addUser(
            adminActor,
            firstName,
            lastName,
            wikiName
        )

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(error)
    }
}