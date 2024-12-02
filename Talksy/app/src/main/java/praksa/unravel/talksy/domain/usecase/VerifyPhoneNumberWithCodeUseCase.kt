package praksa.unravel.talksy.domain.usecase


import com.google.firebase.auth.PhoneAuthCredential
import praksa.unravel.talksy.data.repositories.AuthRepository

class VerifyPhoneNumberWithCodeUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(verificationId: String, code: String): PhoneAuthCredential {
        return repository.verifyPhoneNumberWithCode(verificationId, code)
    }
}
