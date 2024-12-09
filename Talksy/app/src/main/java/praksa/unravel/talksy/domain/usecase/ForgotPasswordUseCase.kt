package praksa.unravel.talksy.domain.usecase

import praksa.unravel.talksy.data.repositories.AuthRepository
import praksa.unravel.talksy.common.exception.Result

class ForgotPasswordUseCase(private val repository: AuthRepository) {
    suspend fun invoke(email: String): Result<Unit> {
        return repository.sendPasswordResetEmail(email)
    }
}
