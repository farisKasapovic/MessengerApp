package praksa.unravel.talksy.domain.usecase

import com.facebook.AccessToken
import praksa.unravel.talksy.data.repositories.AuthRepository

class LoginWithFacebookUseCase(private val repository: AuthRepository) {
    suspend fun invoke(token: AccessToken):Boolean{
        return repository.loginWithFacebook(token)
    }
}