package praksa.unravel.talksy.domain.usecase

import com.google.firebase.auth.PhoneAuthCredential
import praksa.unravel.talksy.data.repositories.AuthRepository

class LinkPhoneNumberUseCase(private val repository: AuthRepository) {
    suspend fun invoke(credential: PhoneAuthCredential): Boolean {
        return repository.linkPhoneNumber(credential)
    }
}