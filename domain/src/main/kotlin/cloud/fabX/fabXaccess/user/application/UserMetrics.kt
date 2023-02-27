package cloud.fabX.fabXaccess.user.application

import cloud.fabX.fabXaccess.common.model.SystemActor
import cloud.fabX.fabXaccess.user.model.UserRepository

class UserMetrics(
    private val userRepository: UserRepository
) {
    suspend fun getUserAmount(
        actor: SystemActor
    ): Int {
        return userRepository.getAll().size
    }
}