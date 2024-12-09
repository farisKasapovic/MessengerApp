package praksa.unravel.talksy.domain.usecases

import praksa.unravel.talksy.data.repositories.AuthRepository
import javax.inject.Inject

class DeleteUserFromAuthUseCase @Inject constructor(private val authRepository: AuthRepository) {

    suspend fun invoke() {
        try {
            authRepository.deleteUser()
        } catch (e: Exception) {
            throw Exception("DeleteUserUseCase failed: ${e.message}")
        }
    }
}
