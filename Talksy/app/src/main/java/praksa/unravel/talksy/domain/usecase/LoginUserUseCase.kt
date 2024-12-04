package praksa.unravel.talksy.domain.usecase

import praksa.unravel.talksy.data.repositories.AuthRepository

class LoginUserUseCase(private val authRepository: AuthRepository) {
    suspend fun invoke(email: String, password: String): Boolean {
        return authRepository.loginUserWithEmail(email, password)
    }
}