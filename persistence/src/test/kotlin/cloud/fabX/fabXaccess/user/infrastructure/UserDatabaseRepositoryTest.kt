package cloud.fabX.fabXaccess.user.infrastructure

import assertk.assertThat
import cloud.fabX.fabXaccess.user.model.UserId
import java.util.UUID
import kotlin.test.Test

internal class UserDatabaseRepositoryTest {
    @Test
    fun `given empty repository when getting user by id then returns user not found error`() {
        // given
        val repository = UserDatabaseRepository()

        // when
        val result = repository.getById(UserId(UUID.randomUUID()))

        // then
        assertThat(result)
            .isRight()
    }

    @Test
    fun `given events for user stored in repository when getting user by id then returns user from events`() {
        TODO()
    }

    @Test
    fun `given events for user stored in repository when storing then accepts aggregate version number increased by one`() {
        TODO()
    }

    @Test
    fun `given events for user stored in repository when storing then not accepts version numbers other than increased by one`() {
        TODO()
    }
}