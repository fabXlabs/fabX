package cloud.fabX.fabXaccess.user.application

import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.user.model.Admin
import cloud.fabX.fabXaccess.user.model.AdminFixture
import cloud.fabX.fabXaccess.user.model.UserFixture
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@MockitoSettings
internal class GettingSoftDeletedUsersTest {

    private val adminActor: Admin = AdminFixture.arbitrary()

    private val correlationId = CorrelationIdFixture.arbitrary()

    private lateinit var logger: Logger
    private lateinit var gettingSoftDeletedUsers: cloud.fabX.fabXaccess.user.model.GettingSoftDeletedUsers

    private lateinit var testee: GettingSoftDeletedUsers

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock gettingSoftDeletedUsers: cloud.fabX.fabXaccess.user.model.GettingSoftDeletedUsers
    ) {
        this.logger = logger
        this.gettingSoftDeletedUsers = gettingSoftDeletedUsers

        testee = GettingSoftDeletedUsers({ logger }, gettingSoftDeletedUsers)
    }

    @Test
    fun `when getting soft deleted users then returns soft deleted users`() = runTest {
        // given
        val user1 = UserFixture.arbitrary()
        val user2 = UserFixture.arbitrary()

        whenever(gettingSoftDeletedUsers.getSoftDeleted())
            .thenReturn(setOf(user1, user2))

        // when
        val result = testee.getSoftDeletedUsers(adminActor, correlationId)

        // then
        assertThat(result)
            .containsExactlyInAnyOrder(user1, user2)
    }
}