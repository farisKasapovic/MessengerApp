package praksa.unravel.talksy.domain.usecase

import praksa.unravel.talksy.data.repositories.AuthRepository
import praksa.unravel.talksy.common.exception.Result

class LoginUserUseCase(private val authRepository: AuthRepository) {
    suspend fun invoke(email: String, password: String): Result<Boolean> {
        return authRepository.loginUserWithEmail(email, password)
    }
}