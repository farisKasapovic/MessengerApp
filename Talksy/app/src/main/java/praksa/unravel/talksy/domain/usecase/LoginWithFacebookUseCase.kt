package praksa.unravel.talksy.domain.usecase

import com.facebook.AccessToken
import praksa.unravel.talksy.data.repositories.AuthRepository
import praksa.unravel.talksy.common.exception.Result

class LoginWithFacebookUseCase(private val repository: AuthRepository) {
    suspend fun invoke(token: AccessToken): Result<Boolean> {
        return repository.loginWithFacebook(token)
    }
}