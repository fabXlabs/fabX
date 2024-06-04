package cloud.fabX.fabXaccess.user.application

import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isSameInstanceAs
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.ErrorFixture
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.model.SystemActor
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.model.UsernamePasswordIdentity
import isLeft
import isRight
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.whenever

@MockitoSettings
internal class GettingUserByIdentityTest {
    private val correlationId = CorrelationIdFixture.arbitrary()

    private lateinit var logger: Logger
    private lateinit var gettingUserByIdentity: cloud.fabX.fabXaccess.user.model.GettingUserByIdentity
    private lateinit var testee: GettingUserByIdentity

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock gettingUserByIdentity: cloud.fabX.fabXaccess.user.model.GettingUserByIdentity
    ) {
        this.logger = logger
        this.gettingUserByIdentity = gettingUserByIdentity

        testee = GettingUserByIdentity({ logger }, gettingUserByIdentity)
    }

    @Nested
    internal inner class GivenCorrelationId {
        @Test
        fun `when getting by identity then returns user from repository`() = runTest {
            // given
            val identity = UsernamePasswordIdentity(
                "some.one",
                "h6dLJxkiB+4phuKyqiNC1JBKI+2BXYWq0R8eLiBOYIg="
            )
            val user = UserFixture.arbitrary()

            whenever(gettingUserByIdentity.getByIdentity(identity))
                .thenReturn(user.right())

            // when
            val result = testee.getUserByIdentity(
                SystemActor,
                correlationId,
                identity
            )

            // then
            assertThat(result)
                .isRight()
                .isSameInstanceAs(user)
        }

        @Test
        fun `given user not exists when getting by identity then returns error`() = runTest {
            // given
            val identity = UsernamePasswordIdentity(
                "some.one",
                "h6dLJxkiB+4phuKyqiNC1JBKI+2BXYWq0R8eLiBOYIg="
            )

            val error = ErrorFixture.arbitrary()

            whenever(gettingUserByIdentity.getByIdentity(identity))
                .thenReturn(error.left())

            // when
            val result = testee.getUserByIdentity(
                SystemActor,
                correlationId,
                identity
            )

            // then
            assertThat(result)
                .isLeft()
                .isSameInstanceAs(error)
        }
    }

    @Nested
    internal inner class GivenNoCorrelationId {
        @Test
        fun `when getting by identity then returns user from repository`() = runTest {
            // given
            val identity = UsernamePasswordIdentity(
                "some.one",
                "h6dLJxkiB+4phuKyqiNC1JBKI+2BXYWq0R8eLiBOYIg="
            )
            val user = UserFixture.arbitrary()

            whenever(gettingUserByIdentity.getByIdentity(identity))
                .thenReturn(user.right())

            // when
            val result = testee.getUserByIdentity(
                SystemActor,
                identity
            )

            // then
            assertThat(result)
                .isRight()
                .isSameInstanceAs(user)
        }

        @Test
        fun `given user not exists when getting by identity then returns error`() = runTest {
            // given
            val identity = UsernamePasswordIdentity(
                "some.one",
                "h6dLJxkiB+4phuKyqiNC1JBKI+2BXYWq0R8eLiBOYIg="
            )

            val error = ErrorFixture.arbitrary()

            whenever(gettingUserByIdentity.getByIdentity(identity))
                .thenReturn(error.left())

            // when
            val result = testee.getUserByIdentity(
                SystemActor,
                identity
            )

            // then
            assertThat(result)
                .isLeft()
                .isSameInstanceAs(error)
        }
    }
}