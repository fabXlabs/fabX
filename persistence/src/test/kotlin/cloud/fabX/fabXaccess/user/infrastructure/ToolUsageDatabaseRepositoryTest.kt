package cloud.fabX.fabXaccess.user.infrastructure

import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEmpty
import cloud.fabX.fabXaccess.common.infrastructure.withTestApp
import cloud.fabX.fabXaccess.tool.model.ToolIdFixture
import cloud.fabX.fabXaccess.user.model.ToolUsageLogEntry
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import kotlin.time.Duration.Companion.seconds
import kotlinx.datetime.Clock
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.kodein.di.instance

internal class ToolUsageDatabaseRepositoryTest {
    private val userId1 = UserIdFixture.arbitrary()
    private val userId2 = UserIdFixture.arbitrary()
    private val toolId1 = ToolIdFixture.arbitrary()
    private val toolId2 = ToolIdFixture.arbitrary()
    private val fixedInstant1 = Clock.System.now().minus(2.seconds)
    private val fixedInstant2 = Clock.System.now().minus(1.seconds)
    private val fixedInstant3 = Clock.System.now()

    internal fun withRepository(block: suspend (ToolUsageLogDatabaseRepository) -> Unit) = withTestApp { di ->
        val repository: ToolUsageLogDatabaseRepository by di.instance()
        block(repository)
    }

    @Test
    fun `given empty repository when getting all then returns empty list`() = withRepository { repository ->
        // given

        // when
        val result = repository.getAll()

        // then
        assertThat(result)
            .isEmpty()
    }

    @Nested
    internal inner class GivenUsagesStoredInRepository {
        private val usage1 = ToolUsageLogEntry(
            fixedInstant1,
            userId1,
            toolId1
        )

        private val usage2 = ToolUsageLogEntry(
            fixedInstant2,
            userId1,
            toolId2
        )

        private val usage3 = ToolUsageLogEntry(
            fixedInstant3,
            userId2,
            toolId2
        )

        private fun withSetupTestApp(block: suspend (ToolUsageLogDatabaseRepository) -> Unit) =
            withRepository { repository ->
                repository.store(usage1)
                repository.store(usage2)
                repository.store(usage3)

                block(repository)
            }

        @Test
        fun `when getting all usages then returns all usages`() = withSetupTestApp { repository ->
            // given

            // when
            val result = repository.getAll()

            // then
            assertThat(result).containsExactlyInAnyOrder(
                usage1,
                usage2,
                usage3
            )
        }
    }
}