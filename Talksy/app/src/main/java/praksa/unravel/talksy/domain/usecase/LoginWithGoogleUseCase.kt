package praksa.unravel.talksy.domain.usecase

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.flow.Flow
import praksa.unravel.talksy.data.repositories.AuthRepository
import praksa.unravel.talksy.common.result.Result
import javax.inject.Inject

class LoginWithGoogleUseCase @Inject constructor(private val authRepository: AuthRepository) {
    operator fun invoke(account: GoogleSignInAccount): Flow<Result<Boolean>> {
        return authRepository.loginWithGoogle(account)
    }
}