package praksa.unravel.talksy.domain.usecase

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import praksa.unravel.talksy.data.repositories.AuthRepository

class LoginWithGoogleUseCase(private val authRepository: AuthRepository) {
    suspend fun invoke(account: GoogleSignInAccount): Boolean {
        return authRepository.loginWithGoogle(account)
    }
}