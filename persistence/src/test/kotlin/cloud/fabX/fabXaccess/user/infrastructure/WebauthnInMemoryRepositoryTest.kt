package cloud.fabX.fabXaccess.user.infrastructure

import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.infrastructure.withTestApp
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import isLeft
import isRight
import org.junit.jupiter.api.Test
import org.kodein.di.instance

internal class WebauthnInMemoryRepositoryTest {
    private val userId = UserIdFixture.static(5678)

    @Test
    fun `given empty repository when getting challenge then returns challenge not found`() = withTestApp { di ->
        // given
        val repository: WebauthnInMemoryRepository by di.instance()

        // when
        val result = repository.getChallenge(userId)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.ChallengeNotFound(
                    "Challenge not found for UserId(value=5d10bf13-ef2a-3d2a-acf3-b9e9a0e9537c).",
                    userId
                )
            )
    }

    @Test
    fun `given challenge stored in repository when getting challenge then returns challenge`() = withTestApp { di ->
        // given
        val repository: WebauthnInMemoryRepository by di.instance()
        val challenge = byteArrayOf(1, 2, 3, 4, 5)
        repository.storeChallenge(userId, challenge)

        // when
        val result = repository.getChallenge(userId)

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(challenge)
    }

    @Test
    fun `given challenge stored and gotten when getting challenge then returns challenge not found`() =
        withTestApp { di ->
            // given
            val repository: WebauthnInMemoryRepository by di.instance()
            val challenge = byteArrayOf(1, 2, 3, 4, 5)
            repository.storeChallenge(userId, challenge)
            repository.getChallenge(userId)

            // when
            val result = repository.getChallenge(userId)

            // then
            assertThat(result)
                .isLeft()
                .isEqualTo(
                    Error.ChallengeNotFound(
                        "Challenge not found for UserId(value=5d10bf13-ef2a-3d2a-acf3-b9e9a0e9537c).",
                        userId
                    )
                )
        }
}