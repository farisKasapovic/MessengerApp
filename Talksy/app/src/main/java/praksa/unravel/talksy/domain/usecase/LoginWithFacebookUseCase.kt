package praksa.unravel.talksy.domain.usecase

import com.facebook.AccessToken
import kotlinx.coroutines.flow.Flow
import praksa.unravel.talksy.data.repositories.AuthRepository
import praksa.unravel.talksy.common.result.Result
import javax.inject.Inject

class LoginWithFacebookUseCase @Inject constructor(private val repository: AuthRepository) {
     operator fun invoke(token: AccessToken): Flow<Result<Boolean>> {
        return repository.loginWithFacebook(token)
    }
}