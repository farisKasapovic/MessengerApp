package praksa.unravel.talksy.domain.usecase

import com.facebook.AccessToken
import praksa.unravel.talksy.data.repositories.AuthRepository
import praksa.unravel.talksy.common.result.Result
import javax.inject.Inject

class LoginWithFacebookUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend fun invoke(token: AccessToken): Result<Boolean> {
        return repository.loginWithFacebook(token)
    }
}