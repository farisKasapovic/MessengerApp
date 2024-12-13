package praksa.unravel.talksy.domain.usecase

import kotlinx.coroutines.flow.Flow
import praksa.unravel.talksy.data.repositories.AuthRepository
import praksa.unravel.talksy.common.result.Result
import javax.inject.Inject

class LoginUserUseCase @Inject constructor(private val authRepository: AuthRepository) {
     operator fun invoke(email: String, password: String): Flow<Result<Boolean>> {
        return authRepository.loginUserWithEmail(email, password)
    }
}